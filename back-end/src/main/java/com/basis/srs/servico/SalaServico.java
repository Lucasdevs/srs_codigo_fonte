package com.basis.srs.servico;

import com.basis.srs.dominio.Equipamento;
import com.basis.srs.dominio.Sala;
import com.basis.srs.dominio.SalaEquipamento;
import com.basis.srs.repositorio.EquipamentoRepositorio;
import com.basis.srs.repositorio.SalaEquipamentoRepositorio;
import com.basis.srs.repositorio.SalaRepositorio;
import com.basis.srs.servico.dto.SalaDTO;
import com.basis.srs.servico.dto.SalaEquipamentoDTO;
import com.basis.srs.servico.exception.RegraNegocioException;
import com.basis.srs.servico.mapper.EquipamentoMapper;
import com.basis.srs.servico.mapper.SalaEquipamentoMapper;
import com.basis.srs.servico.mapper.SalaMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SalaServico {

    private final SalaMapper salaMapper;
    private final SalaRepositorio salaRepositorio;
    private final SalaEquipamentoRepositorio salaEquipamentoRepositorio;
    private final EquipamentoRepositorio equipamentoRepositorio;
    private final SalaEquipamentoMapper salaEquipamentoMapper;
    private final EquipamentoMapper equipamentoMapper;

    //GET
    public List<SalaDTO> listarTodas() {
        List<Sala> salas = salaRepositorio.findAll();
        List<SalaDTO> salasDto = salaMapper.toDto(salas);
        return salasDto;
    }

    //GET POR ID
    public SalaDTO pegarSalaPorId(Integer id) {
        Sala sala = salaRepositorio.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Sala não encontrada!"));
        SalaDTO salaDto = salaMapper.toDto(sala);
        return salaDto;
    }

    //POST e put
    public SalaDTO salvar(SalaDTO salaDto) {
        // CHECAR SE É UMA ATUALIZAÇÃO
        if (salaDto.getId() != null) {
            validaAtualizacao(salaDto);
        }
        Sala sala2 = salaMapper.toEntity(salaDto);
        List<SalaEquipamento> novosEquipamentos = sala2.getEquipamentos();
        sala2.setEquipamentos(new ArrayList<>());
        salaRepositorio.save(sala2);
        novosEquipamentos.forEach(equipamento -> {
            equipamento.setSala(sala2);
            equipamento.getId().setIdSala(sala2.getId());
        });
        salaEquipamentoRepositorio.saveAll(novosEquipamentos);
        sala2.setEquipamentos(novosEquipamentos);
        return salaMapper.toDto(sala2);
    }

    private void validaAtualizacao(SalaDTO salaDto) {
        //PEGAR A ANTIGA SALA, QUE O USUÁRIO QUER ATUALIZAR
        Sala sala = salaRepositorio.findById(salaMapper.toEntity(salaDto).getId()).orElse(null);
        //PEGAR A LISTA DE EQUIPAMENTOS PASSADOS NA SALA NOVA
        List<SalaEquipamentoDTO> salaEquipamentoAtualizados = salaDto.getEquipamentos();
        //PEGAR A LISTA DE EQUIPAMENTOS EXISTENTES NA SALA JÁ CADASTRADA
        List<SalaEquipamento> salaEquipamentoAntigos = sala.getEquipamentos();
        for (int i = 0; i < salaEquipamentoAntigos.size(); i++) {
            Equipamento equipamento = equipamentoRepositorio.findById(salaEquipamentoAntigos.get(i).getEquipamento().getId()).orElse(null);
            if (equipamento.getObrigatorio() == 1) {
                for (int j = 0; j < salaEquipamentoAtualizados.size(); j++) {
                    if (salaEquipamentoAtualizados.get(j).getIdEquipamento() == equipamento.getId()) {
                        if (salaEquipamentoAtualizados.get(j).getQuantidade() < 1) {
                            throw new RegraNegocioException("A quantidade mínima para esse equipamento nesta sala é 1!");
                        }
                    }
                    if (salaEquipamentoAtualizados.size() == j) {
                        throw new RegraNegocioException("Você excluiu um equipamento obrigatório!");
                    }
                }
            }
        }
    }

    //DELETE POR ID
    public void deletarSala (Integer id) {

        Sala sala = salaRepositorio.findById(id).orElseThrow(() -> new RegraNegocioException(id + ": Essa sala não existe."));
        if (sala.getDisponivel() == 0) {
            throw new RegraNegocioException("Essa sala não pode ser deletada, pois há uma reserva em andamento nela.");
        }
        else {
            salaEquipamentoRepositorio.deleteAllBySalaId(id);
            salaRepositorio.deleteById(id);
        }
    }
}
