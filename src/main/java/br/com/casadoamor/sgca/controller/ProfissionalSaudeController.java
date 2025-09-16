package br.com.casadoamor.sgca.controller;

import br.com.casadoamor.sgca.dto.ProfissionalSaudeDto;
import br.com.casadoamor.sgca.dto.ProfissionalSaudeRequestJson;
import br.com.casadoamor.sgca.service.ProfissionalSaudeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/1.0", produces = "application/json;charset=UTF-8")
public class ProfissionalSaudeController {

    @Autowired
    private ProfissionalSaudeService profissionalSaudeService;

    @PostMapping(path="/profissional-saude")
    public ResponseEntity<ProfissionalSaudeDto> createProfissionalSaude(@RequestBody ProfissionalSaudeRequestJson profissionalSaude) {
        return new ResponseEntity<>(this.profissionalSaudeService.createProfissionalSaude(profissionalSaude), HttpStatus.CREATED);
    }

    @GetMapping(path="/profissional-saude/{id}")
    public ResponseEntity<ProfissionalSaudeDto> getProfissionalSaudeById(@PathVariable Long id) {
        return new ResponseEntity<>(this.profissionalSaudeService.getProfissionalSaudeById(id), HttpStatus.OK);
    }

    @GetMapping(path = "/profissional-saude")
    public ResponseEntity<List<ProfissionalSaudeDto>> getAllProfissionalSaude() {
        return new ResponseEntity<>(this.profissionalSaudeService.getAllProfissionalSaude(), HttpStatus.OK);
    }

    @PutMapping(path="/profissional-saude/{id}")
    public ResponseEntity<ProfissionalSaudeDto> updateProfissionalSaude(
            @PathVariable Long id,
            @RequestBody ProfissionalSaudeRequestJson profissionalSaude
    ){
        return new ResponseEntity<>(this.profissionalSaudeService.updateProfissionalSaude(id, profissionalSaude), HttpStatus.OK);
    }

    @DeleteMapping(path="/profissional-saude/{id}")
    public ResponseEntity<ProfissionalSaudeDto> deleteProfissionalSaude(@PathVariable Long id) {
        this.profissionalSaudeService.deleteProfissionalSaude(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
