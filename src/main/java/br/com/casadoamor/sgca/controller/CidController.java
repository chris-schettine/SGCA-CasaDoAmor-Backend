package br.com.casadoamor.sgca.controller;

import br.com.casadoamor.sgca.dto.CidDto;
import br.com.casadoamor.sgca.service.CidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/1.0", produces = "application/json;charset=UTF-8")
public class CidController {

    @Autowired
    private CidService cidService;

    @GetMapping(path="/cid/{id:[a-zA-Z0-9.]+}")
    public ResponseEntity<List<CidDto>> getCidById(@PathVariable String id) {
        return new ResponseEntity<>(this.cidService.getCidById(id), HttpStatus.OK);
    }

    @GetMapping(path="/cid/{descricao}/descricao")
    public ResponseEntity<List<CidDto>> getCidByDesc(@PathVariable String descricao) {
        return new ResponseEntity<>(this.cidService.getCidByDescricao(descricao), HttpStatus.OK);
    }

    @GetMapping(path = "/cid")
    public ResponseEntity<List<CidDto>> getAllCid() {
        return new ResponseEntity<>(this.cidService.getAllCid(), HttpStatus.OK);
    }
}
