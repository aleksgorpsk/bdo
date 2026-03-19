package ag.com.dbo.services;

import ag.com.dbo.models.Etl;
import ag.com.dbo.repositories.EtlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @CachePut(value = "etl", key = "#etl.id")
    public boolean create(Etl etl) {
            Etl result = etlRepository.save(etl);
            log.info("Service: result "+result);
            return true;

    }

    public Page<Etl>  retrieveAll(PageRequest pageable){
        Page<Etl> res = etlRepository.findAll(pageable);
        return res;
    }
    /*
     * -------------------------------------------------------------------------
     * Retrieve
     * -------------------------------------------------------------------------
     */

    @Cacheable(value = "etl", key = "#id")
    public Etl retrieveById(BigInteger id) {
        return etlRepository.findById(id)
                .orElse(null);
    }

    @Cacheable(value = "etl")
    public List<Etl> retrieveAll() {

        log.info("retrieveAll");
        return StreamSupport.stream(etlRepository.findAll().spliterator(), false)
//                .map(this::mapFrom)
                .peek(x-> log.info("etl:"+ x.toString()))
                .toList();
    }

    /*
     * -------------------------------------------------------------------------
     * Search
     * -------------------------------------------------------------------------
     */
/*
    public List<EtlDTO> searchByDescription(String keyword) {
        return etlRepository.fi.findByDescriptionContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapFrom)
                .toList();
    }
*/
    /*
     * -------------------------------------------------------------------------
     * Update
     * -------------------------------------------------------------------------
     */

    @CachePut(value = "elt", key = "#etl.id")
    public boolean update(Etl etl) {
        if (etlRepository.existsById(etl.getId())) {
//            Etl etl = mapFrom(etlDTO);
            etlRepository.save(etl);
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

    @CacheEvict(value = "etl", key = "#id")
    public boolean delete(BigInteger id) {
        if (etlRepository.existsById(id)) {
            etlRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

}
