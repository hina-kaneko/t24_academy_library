package jp.co.metateam.library.service;

import java.time.LocalDate;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.repository.RentalManageRepository;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final RentalManageRepository rentalManageRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository,
            RentalManageRepository rentalManageRepository) {
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
        this.rentalManageRepository = rentalManageRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }

    @Transactional
    public List<Stock> findStockAvailableAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional
    public List<BookMst> findByActiveAllBook() {
        return this.bookMstRepository.findByActiveAllBook();
    }

    @Transactional
    public List<Stock> findByStatusAvailableAllStock(Long bookId) {
        return this.stockRepository.findByStatusAvailableAllStock(bookId);
    }

    @Transactional
    public long countfindByRentalWaitDateAndId(Date date, List<String> stockId) {
        return this.rentalManageRepository.countfindByRentalWaitDateAndId(date, stockId);
    }

    @Transactional
    public long countfindByRentalingDateAndId(Date date, List<String> stockId) {
      List<RentalManage> unavailableStockLists = this.rentalManageRepository.countfindByRentalingDateAndId(date, stockId);
      long unavailableStockNum =unavailableStockLists.size();

      return unavailableStockNum;
    }

    @Transactional
    public  List<Stock> findAllAvailableStockList() {
        return this.stockRepository.findAllAvailableStockList();
    }

    @Transactional
    public List<Stock> findAllUnAvailableStockList() {
        return this.stockRepository.findAllUnAvailableStockList();
    }

    @Transactional
    public void save(StockDto stockDto) throws Exception {
        try {
            Stock stock = new Stock();
            BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setBookMst(bookMst);
            stock.setId(stockDto.getId());
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public void update(String id, StockDto stockDto) throws Exception {
        try {
            Stock stock = findById(id);
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            BookMst bookMst = stock.getBookMst();
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setId(stockDto.getId());
            stock.setBookMst(bookMst);
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }
    
    public List<List<Object>> generateValues(Integer year, Integer month, Integer daysInMonth) {
        List<List<Object>>someValues = new ArrayList<>();

        List<BookMst>bookData = findByActiveAllBook();      
           
        for (BookMst bookLoop : bookData){
             String  title = bookLoop.getTitle();
        
            List<Stock> availableStockCount = findByStatusAvailableAllStock(bookLoop.getId());
            String stockId = String.valueOf(availableStockCount.size());
            //利用可能在庫を追加
            List<String> stockNum = new ArrayList<>();
            for (Stock stockLoop : availableStockCount){
                stockNum.add(stockLoop.getId());        
            }
            
            ///日ごとの件数を入れるリスト作成
            List<String> dayNum = new ArrayList<>();

            //for(日付分ループ 指定の月の日付分){
            for(int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++){
                LocalDate localDate = LocalDate.of(year,month,dayOfMonth);
                //Dateに変換
                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                
                Long scheduledRentaWaitData = countfindByRentalWaitDateAndId(date,stockNum);
                Long scheduledRentalingData = countfindByRentalingDateAndId (date,stockNum);

                Long dayNums = stockNum.size() - (scheduledRentaWaitData + scheduledRentalingData);

                if (dayNums == 0) {
                    dayNum.add("x");
                } else {
                    dayNum.add(String.valueOf(dayNums));

                }
            }
            List<Object> bookinfo = new ArrayList<>();

            bookinfo.add(title);
            bookinfo.add(stockId);
            bookinfo.add(dayNum);

            someValues.add(bookinfo);        
                     
        }
        return someValues;
    }
    
}



