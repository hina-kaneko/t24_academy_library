package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    List<RentalManage> findAll();

	Optional<RentalManage> findById(Long id);

    @Query("SELECT COUNT(rm) FROM RentalManage rm WHERE rm.expectedRentalOn <= ?1 AND ?1<= rm.expectedReturnOn AND rm.stock.Id IN(?2) AND rm.status = 0")
    long countfindByRentalWaitDateAndId(Date date, List<String> stockId);
 
    @Query(value ="SELECT * FROM rental_manage as rm WHERE CAST(rm.rentaled_At as date) <= :date AND :date <= rm.expected_return_on AND rm.stock_id IN(:stockId) AND rm.status= 1",nativeQuery = true)
    List<RentalManage> countfindByRentalingDateAndId(Date date, List<String> stockId);

    //在庫管理番号に基づく、貸出待ち（０）と貸出中（１）のデータを持ってくる
    @Query("SELECT r FROM RentalManage r WHERE r.stock.id =?1 AND r.status in (0,1)") 
     List<RentalManage> findByStockIdAndStatus(String newStock_id); 

}
