package ag.com.dbo.services;

import ag.com.dbo.models.Etl;
import ag.com.dbo.models.EtlDTO;
import ag.com.dbo.repositories.EtlRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class EtlService {

    private final EtlRepository etlRepository;
    private final ModelMapper modelMapper;

    /*
     * -------------------------------------------------------------------------
     * Create
     * -------------------------------------------------------------------------
     */

    @CachePut(value = "etls", key = "#etlDTO.id")
    public EtlDTO create(EtlDTO etlDTO) {
            Etl etl = mapFrom(etlDTO);
            Etl newEtl =etlRepository.saveAndFlush(etl);
            return mapFrom(newEtl);
    }

    /*
     * -------------------------------------------------------------------------
     * Retrieve
     * -------------------------------------------------------------------------
     */

    @Cacheable(value = "etls", key = "#id")
    public EtlDTO retrieveById(BigInteger id) {
        return etlRepository.findById(id)
                .map(this::mapFrom)
                .orElse(null);
    }



    public Page<@NonNull EtlDTO> retrievePage(PageRequest pageable){
        Page<@NonNull Etl> entities = etlRepository.findAll(pageable);
        return entities.map(e-> modelMapper.map(e, EtlDTO.class));

    }

    public List<EtlDTO> findByStatus(Integer status){
        return etlRepository.findByStatus(status).stream()
                .map(this::mapFrom)
                .peek(x-> log.info("etl by status:"+ x.toString()))
                .toList();

    }

    @Cacheable(value = "etls")
    public List<EtlDTO> retrieveAll() {
        return etlRepository.findAll().stream()
                .map(this::mapFrom)
                .peek(x-> log.info("etl:"+ x.toString()))
                .toList();
    }

    @Cacheable(value = "etls")
    public List<EtlDTO> retrievePage() {

        log.info("retrievePage");
        return StreamSupport.stream(etlRepository.findAll().spliterator(), false)
                .map(this::mapFrom)
                .peek(x-> log.info("etl:"+ x.toString()))
                .toList();
    }



    /*
     * -------------------------------------------------------------------------
     * Update
     * -------------------------------------------------------------------------
     */

    @CachePut(value = "etl", key = "#etlDTO.id")
    public boolean update(EtlDTO etlDTO) {
        if (etlRepository.existsById(etlDTO.getId())) {
            etlRepository.save(mapFrom(etlDTO));
            return true;
        } else {
            return false;
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Delete
     * -------------------------------------------------------------------------
     */

    @CacheEvict(value = "etls", key = "#id")
    public boolean delete(BigInteger id) {
        if (etlRepository.existsById(id)) {
            etlRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }


    private EtlDTO mapFrom(Etl etl) {
        return modelMapper.map(etl, EtlDTO.class);
    }

    private Etl mapFrom(EtlDTO dto) {
        return modelMapper.map(dto, Etl.class);
    }

}
