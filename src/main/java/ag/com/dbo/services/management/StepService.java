package ag.com.dbo.services.management;

import ag.com.dbo.models.management.StepDTO;

import java.math.BigInteger;



public interface StepService {

    /*
     * -------------------------------------------------------------------------
     * Update
     * -------------------------------------------------------------------------
     */

    public boolean update(StepDTO stepDTO) ;

    /*
     * -------------------------------------------------------------------------
     * Create
     * -------------------------------------------------------------------------
     */

    public StepDTO create(StepDTO stepDTO) ;

    /**
     *
     * Retrieve by id
     *
     */

    public StepDTO retrieveById(BigInteger id) ;

    /**
     * delete step by id
     * @param id
     */
    public void delete(BigInteger id) ;


}
