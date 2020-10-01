package com.basis.srs.servico;

import com.basis.srs.dominio.Sala;
import com.basis.srs.repositorio.SalaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SalaService {

    private SalaRepositorio sr;

    //Post
    public void cadastrarSala(Sala s){
    }

    //Put
    public void alterarSala(Sala s, Integer id) {
    }

    //Get
    public void pegarSalaPorId() {
    }

    //Get por ID
    public void listarTodas(Integer id) {
    }

    //Delete por ID
    public void deletarSala(Integer id) {
    }
}
