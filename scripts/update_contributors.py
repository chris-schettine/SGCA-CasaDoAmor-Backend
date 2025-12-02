#!/usr/bin/env python3
import os
import sys
import json
import base64
import urllib.request
from datetime import datetime

GITHUB_REPO = os.environ.get('GITHUB_REPOSITORY')
GITHUB_TOKEN = os.environ.get('GITHUB_TOKEN')
README_PATH = 'README.md'

if not GITHUB_REPO or not GITHUB_TOKEN:
    print('GITHUB_REPOSITORY and GITHUB_TOKEN must be set', file=sys.stderr)
    sys.exit(2)

API_URL = f'https://api.github.com/repos/{GITHUB_REPO}/contributors?per_page=100'
req = urllib.request.Request(API_URL)
req.add_header('Authorization', f'token {GITHUB_TOKEN}')
req.add_header('Accept', 'application/vnd.github.v3+json')

with urllib.request.urlopen(req) as resp:
    data = json.load(resp)

# data is a list of contributor objects with 'login', 'contributions', 'avatar_url', 'html_url'
contributors = [
    {
        'login': c.get('login') or c.get('name') or 'unknown',
        'contributions': c.get('contributions', 0),
        'avatar_url': c.get('avatar_url'),
        'html_url': c.get('html_url')
    }
    for c in data
]

# sort by contributions desc
contributors.sort(key=lambda x: x['contributions'], reverse=True)

# generate simple SVG bar chart
max_commits = max([c['contributions'] for c in contributors]) if contributors else 1
width = 700
left = 180
bar_area = width - left - 20
bar_height = 20
gap = 8
height = (bar_height + gap) * len(contributors) + 40

svg_lines = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">']
svg_lines.append('<style>text{font-family:Arial, Helvetica, sans-serif;font-size:12px;}</style>')
svg_lines.append(f'<rect width="100%" height="100%" fill="#ffffff00"/>')

y = 20
for c in contributors:
    val = c['contributions']
    w = int((val / max_commits) * bar_area) if max_commits else 0
    # avatar (circle)
    svg_lines.append(f'<image href="{c["avatar_url"]}" x="6" y="{y-4}" height="32" width="32" clip-path="circle(16px at 16px 16px)" />')
    # name
    name = c['login']
    svg_lines.append(f'<text x="46" y="{y+10}" fill="#111">{name}</text>')
    # bar
    svg_lines.append(f'<rect x="{left}" y="{y-12}" width="{w}" height="{bar_height}" fill="#4caf50" rx="3"/>')
    # value
    svg_lines.append(f'<text x="{left + w + 8}" y="{y+10}" fill="#111">{val}</text>')
    y += bar_height + gap

svg_lines.append(f'<text x="6" y="{height-6}" fill="#666" font-size="10">Updated: {datetime.utcnow().isoformat()}Z</text>')
svg_lines.append('</svg>')
svg = '\n'.join(svg_lines)

b64 = base64.b64encode(svg.encode('utf-8')).decode('ascii')
img_md = f'![Contributors](data:image/svg+xml;base64,{b64})'

md_block = f"""
<!-- CONTRIBUTORS_STATS_START -->
### Contribuidores (commits)

{img_md}

_Last updated: {datetime.utcnow().strftime('%Y-%m-%d %H:%M UTC')}_

<!-- CONTRIBUTORS_STATS_END -->
"""

# read README and replace block
with open(README_PATH, 'r', encoding='utf-8') as f:
    content = f.read()

start = '<!-- CONTRIBUTORS_STATS_START -->'
end = '<!-- CONTRIBUTORS_STATS_END -->'
if start in content and end in content:
    before = content.split(start)[0]
    after = content.split(end)[1]
    new_content = before + md_block + after
    if new_content != content:
        with open(README_PATH, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print('README updated')
    else:
        print('No changes needed')
else:
    print('Markers not found in README', file=sys.stderr)
    sys.exit(3)
