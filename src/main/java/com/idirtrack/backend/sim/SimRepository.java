package com.idirtrack.backend.sim;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.idirtrack.backend.operator.Operator;

@Repository
public interface SimRepository extends JpaRepository<Sim, Long>, JpaSpecificationExecutor<Sim> {

        boolean existsByCcid(String ccid);

        @Query("SELECT COUNT(s) > 0 FROM Sim s WHERE s.phone = :phone")
        boolean existsByPhone(@Param("phone") String phone);

        Page<Sim> findAllByStatus(SimStatus pending, Pageable pageRequest);

        @Query("SELECT s FROM Sim s WHERE s.status = :status AND (s.phone LIKE CONCAT('%',:query,'%') OR s.ccid LIKE CONCAT('%',:query,'%'))")
        Page<Sim> findAllByStatusAndPhoneContainingOrCcidContaining(
                        @Param("status") SimStatus status,
                        @Param("query") String query,
                        Pageable pageable);

        long countByStatus(SimStatus pending);

        boolean existsByPhoneAndIdNot(String phone, Long id);

        boolean existsByCcidAndIdNot(String ccid, Long id);


        @Query("SELECT s FROM Sim s WHERE s.phone LIKE %:term% OR s.ccid LIKE %:term% OR s.status LIKE %:term% OR s.puk LIKE %:term% OR s.pin LIKE %:term% OR s.operator.name LIKE %:term%")
        Page<Sim> search(
                        @Param("term") String term,
                        Pageable pageable);

        /**
         * Filter sim by status, operator
         * @param status
         * @param operator
         * @param pageable
         * @return Page<Sim>
         * @see Sim
         */
        @Query("SELECT s FROM Sim s WHERE " +
                        "(:status IS NULL OR s.status = :status) AND " +
                        "(:operator IS NULL OR s.operator = :operator)")
        Page<Sim> filterSim(
                        @Param("status") SimStatus status,
                        @Param("operator") Operator operator,
                        Pageable pageable);

        List<Sim> findAllByStatus(SimStatus nonInstalled);

        // List<Sim> findAllByStatusAndPhoneContainingOrCcidContaining(SimStatus nonInstalled, String query);


}
