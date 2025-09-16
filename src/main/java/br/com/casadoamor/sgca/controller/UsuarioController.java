package br.com.casadoamor.sgca.controller;

import br.com.casadoamor.sgca.dto.UsuarioDto;
import br.com.casadoamor.sgca.dto.UsuarioRequestJson;
import br.com.casadoamor.sgca.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/1.0", produces = "application/json;charset=UTF-8")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping(path="/usuario")
    public ResponseEntity<UsuarioDto> createUsuario(@RequestBody UsuarioRequestJson usuarioRequestJson) {
        return new ResponseEntity<>(this.usuarioService.createUsuario(usuarioRequestJson), HttpStatus.CREATED);
    }

    @GetMapping(path="/usuario/{id}")
    public ResponseEntity<UsuarioDto> getUsuarioById(@PathVariable Long id) {
        return new ResponseEntity<>(this.usuarioService.getUsuarioById(id), HttpStatus.OK);
    }

    @GetMapping(path="/usuario/{cpf:[0-9.-]+}/cpf")
    public ResponseEntity<UsuarioDto> getUsuarioByCpf(@PathVariable String cpf) {
        return new ResponseEntity<>(this.usuarioService.getUsuarioByCpf(cpf), HttpStatus.OK);
    }

    @GetMapping(path = "/usuario")
    public ResponseEntity<List<UsuarioDto>> getAllUsuario() {
        return new ResponseEntity<>(this.usuarioService.getAllUsuario(), HttpStatus.OK);
    }

    @PutMapping(path="/usuario/{id}")
    public ResponseEntity<UsuarioDto> updateUsuario(
            @PathVariable Long id,
            @RequestBody UsuarioDto usuarioDto
    ){
        return new ResponseEntity<>(this.usuarioService.updateUsuario(id, usuarioDto), HttpStatus.OK);
    }

    @DeleteMapping(path="/usuario/{id}")
    public ResponseEntity<UsuarioDto> deleteUsuario(@PathVariable Long id) {
        this.usuarioService.deleteUsuario(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
