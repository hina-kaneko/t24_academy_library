package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


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

     //titleに紐づくbookIdに紐づくstockIdをすべて持ってくる
     @Query("SELECT s.id FROM Stock s JOIN s.bookMst bm WHERE s.bookMst.title = ?1")
     List<String> findByLendableBook(String title);
 
    @Query("SELECT rm.stock.id FROM RentalManage rm WHERE rm.expectedRentalOn <= ?1 AND ?1<= rm.expectedReturnOn AND rm.stock.Id IN(?2) AND rm.status = 0")
    List<String> findRentalWaitStockId(Date date, List<String> stockId);

    @Query(value ="SELECT rm.stock_id FROM rental_manage as rm WHERE CAST(rm.rentaled_At as date) <= :date AND :date <= rm.expected_return_on AND rm.stock_id IN(:stockId) AND rm.status= 1",nativeQuery = true)
    List<String> findRentLingStockId(Date date, List<String> stockId);

    @Query("SELECT s FROM Stock s WHERE s.id IN(?1)")
    List<Stock> findAvailableStockList(List<String> stockIdList);

    //0：利用可に紐づく在庫管理番号を取得
    @Query("SELECT s FROM Stock s WHERE s.status = 0")
    List<Stock> findAllAvailableStockList(Integer status);

    //1：利用不可に紐づく在庫管理番号を取得
    @Query("SELECT s FROM Stock s WHERE s.status = 1")
    List<Stock> findAllUnAvailableStockList(Integer status);

}
