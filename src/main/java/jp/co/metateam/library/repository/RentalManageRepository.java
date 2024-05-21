package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    List<RentalManage> findAll();

	Optional<RentalManage> findById(Long id);
    //在庫管理番号に基づく、貸出待ち（０）と貸出中（１）のデータを持ってくる
    @Query("SELECT r FROM RentalManage r WHERE r.stock.id =?1 AND r.status in (0,1)") 
     List<RentalManage> findByStockIdAndStatus(String newStock_id); 
}
