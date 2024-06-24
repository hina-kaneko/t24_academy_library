package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    
    List<Stock> findAll();

    List<Stock> findByDeletedAtIsNull();

    List<Stock> findByDeletedAtIsNullAndStatus(Integer status);

	Optional<Stock> findById(String id);
    
    List<Stock> findByBookMstIdAndStatus(Long book_id,Integer status);
    
    @Query("SELECT s FROM Stock s WHERE s.status = 0 AND s.bookMst.id = ?1 AND s.deletedAt IS NULL")
    List<Stock> findByStatusAvailableAllStock(Long bookId);

}
