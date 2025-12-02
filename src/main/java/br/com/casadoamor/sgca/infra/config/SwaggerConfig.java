package br.com.casadoamor.sgca.infra.config;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI(@Autowired(required = false) BuildProperties buildProperties,
			@Autowired(required = false) GitProperties gitProperties) {

		String version = "1.0";
		String commitId = null;
		String commitMessage = null;
		String commitUser = null;

		if (buildProperties != null && buildProperties.getVersion() != null) {
			version = buildProperties.getVersion();
		}

		if (gitProperties != null) {
			try {
				commitId = gitProperties.getShortCommitId() != null ? gitProperties.getShortCommitId()
						: gitProperties.getCommitId();
			} catch (Exception ignored) {
			}
		}

		try {
			java.io.InputStream is = getClass().getResourceAsStream("/git.properties");
			if (is != null) {
				java.util.Properties props = new java.util.Properties();
				props.load(is);
				if (commitId == null) {
					commitId = props.getProperty("git.commit.id.abbrev", props.getProperty("git.commit.id"));
				}
				commitMessage = props.getProperty("git.commit.message.short", props.getProperty("git.commit.message.full"));
				commitUser = props.getProperty("git.commit.user.name", props.getProperty("git.commit.user.email"));
			}
		} catch (Exception ex) {
		}

		if (commitId != null && !commitId.isEmpty()) {
			version = commitId;
		}

		String title = "Casa do Amor API";

		StringBuilder description = new StringBuilder("Documentação da API da casa do amor");
		description.append("\n\nCommit: ").append(version);
		if (commitUser != null && !commitUser.isEmpty()) {
			description.append("\n\nAutor: ").append(commitUser);
		}
		if (commitMessage != null && !commitMessage.isEmpty()) {
			description.append("\n\nMensagem: ").append(commitMessage);
		}

		Info info = new Info().title(title).version(version).description(description.toString());
		if (commitUser != null && !commitUser.isEmpty()) {
			info.setContact(new Contact().name(commitUser));
		}

		return new OpenAPI().info(info)
				.addServersItem(new Server().url("http://144.22.182.60:8888"))
				.addServersItem(new Server().url("http://localhost:8080"))
				.addServersItem(new Server().url("http://localhost:8090"))
				.addServersItem(new Server().url("https://casadoamor.duckdns.org"))
				.components(new Components()
						.addSecuritySchemes("bearerAuth",
								new SecurityScheme()
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")
										.in(SecurityScheme.In.HEADER)
										.name("Authorization")))
				.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
	}

	@Bean
	public GroupedOpenApi publicApi(OperationCustomizer parametrizarPaginacaoCustomizada) {
		return GroupedOpenApi.builder()
				.group("public")
				.pathsToMatch("/**")
				.addOperationCustomizer(parametrizarPaginacaoCustomizada)
				.build();
	}

	@Bean
	public GroupedOpenApi agendamentosApi() {
		return GroupedOpenApi.builder()
				.group("agendamentos")
				.pathsToMatch("/api/agendamentos/**")
				.build();
	}

	@Bean
	public GroupedOpenApi pacientesApi() {
		return GroupedOpenApi.builder()
				.group("pacientes")
				.pathsToMatch("/api/pacientes/**")
				.build();
	}

	@Bean
	public GroupedOpenApi acompanhantesApi() {
		return GroupedOpenApi.builder()
				.group("acompanhantes")
				.pathsToMatch("/api/acompanhantes/**")
				.build();
	}

	@Bean
	public OperationCustomizer parametrizarPaginacaoCustomizada() {
		return (operation, handlerMethod) -> {
			for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
				if (Pageable.class.isAssignableFrom(parameter.getParameterType())) {
					operation.addParametersItem(new Parameter()
							.name("page")
							.in("query")
							.description("Número da página (0..N)")
							.required(false)
							.schema(new io.swagger.v3.oas.models.media.IntegerSchema().type("integer")._default(0)));

					operation.addParametersItem(new Parameter()
							.name("size")
							.in("query")
							.description("Quantidade de elementos por página")
							.required(false)
							.schema(new io.swagger.v3.oas.models.media.IntegerSchema().type("integer")._default(10)));

					operation.addParametersItem(new Parameter()
							.name("sort")
							.in("query")
							.description("Critério de ordenação: propriedade,asc|desc. Pode ser usado múltiplas vezes.")
							.required(false)
							.schema(new Schema<String>().type("string"))
							.style(Parameter.StyleEnum.FORM)
							.explode(true));
				}
			}
			return operation;
		};
	}
}
