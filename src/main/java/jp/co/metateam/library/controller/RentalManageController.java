package jp.co.metateam.library.controller;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

import java.text.ParseException;
import java.text.SimpleDateFormat;


import java.util.Date;
 
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.service.AccountService;

import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.model.Stock;

import jp.co.metateam.library.service.StockService;
 
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;
import org.springframework.validation.FieldError;
 
/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {
 
    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;
 
    @Autowired
    public RentalManageController(
        AccountService accountService,
        RentalManageService rentalManageService,
        StockService stockService
    ) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }
 
    /**
     * 貸出一覧画面初期表示
     * @param model
     * @return
     */
    @GetMapping("/rental/index")
    public String index(Model model) {
        // 貸出管理テーブルから全件取得
        List<RentalManage> rentalManageList= this.rentalManageService.findAll();
        // 貸出一覧画面に渡すデータをmodelに追加
        model.addAttribute("rentalManageList",rentalManageList);
        // 貸出一覧画面に遷移
        return "rental/index";
    }
 
    @GetMapping("/rental/add")
        public String add(@RequestParam(required = false) String fig, @RequestParam (required = false) String bookTi, Model model) {
        //テーブルから情報を持ってくる
        List<RentalManage> rentalManageList= this.rentalManageService.findAll();
        List<Stock> stockList = this.stockService.findStockAvailableAll();
        List<Account> accountList= this.accountService.findAll();
        
        //モデル
        model.addAttribute("rentalStatus",RentalStatus.values());
        model.addAttribute("stockList",stockList);
        model.addAttribute("accounts",accountList);
        
 
        if(fig != null && bookTi != null){
            
            try {
                SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date expectedRentalOn = sdFormat.parse(fig);
                RentalManageDto rentalManageDto = new RentalManageDto();
                rentalManageDto.setExpectedRentalOn(expectedRentalOn);
                
                List<Stock> pullDownList = this.rentalManageService.findStockId(bookTi,expectedRentalOn);

                //titleに紐づくbookIdに紐づくstoclIdすべてstock=0、借りられてない本
                model.addAttribute("stockList",pullDownList);
                model.addAttribute("rentalManageDto",rentalManageDto);
            } catch (ParseException e) {
                e.printStackTrace();
            
            }      
        
        } 
        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }
       
 
        return "rental/add";
    }
 
    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }

            //貸出可否チェックスタート
            //今回借りたい本の情報を取得
            Stock stock= this.stockService.findById(rentalManageDto.getStockId());
            //借りたい本が利用可かどうか調べる
            if (stock == null){
            //Stockがnullなら画面に飛ばす
                throw new Exception("Validation error");    
            }
            //Stockのステータスが貸出不可なら画面に飛ばす
            if (stock.getStatus() == 1){
                FieldError fieldError = new FieldError("rentalManageDto", "status", "この本は貸出できません");

                result.addError(fieldError);
 
                throw new Exception("Validation error");
            }
     
            String newStockId = rentalManageDto.getStockId();
            List<RentalManage> rentalManageList = this.rentalManageService.findByStockIdAndStatus(newStockId);

            for(RentalManage list : rentalManageList){
                if(list.getExpectedRentalOn().compareTo(rentalManageDto.getExpectedReturnOn()) <= 0 &&
                 rentalManageDto.getExpectedRentalOn().compareTo(list.getExpectedReturnOn()) <= 0){
                 FieldError fieldError = new FieldError("rentalManageDto", "status", "この期間では登録できません");
                 result.addError(fieldError);

                throw new Exception("Validation error.");
                }
            }    
            /// 貸出可否チェック終了
            
            // 登録処理
            this.rentalManageService.save(rentalManageDto);
 
            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());
 
            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);
 
            return "redirect:/rental/add";
        }
    }
 
    @GetMapping ("/rental/{id}/edit")
     public String edit(@PathVariable("id") String id,Model model) {
        List<Stock> stockList = this.stockService.findStockAvailableAll();
        List<Account> accountList= this.accountService.findAll();
 
        //モデル
        model.addAttribute("rentalStatus",RentalStatus.values());
        model.addAttribute("stockList",stockList);
        model.addAttribute("accounts",accountList);
       
       
           if (!model.containsAttribute("rentalManageDto")) {
              RentalManageDto rentalManageDto = new RentalManageDto();
              RentalManage rentalManage= this.rentalManageService.findById(Long.valueOf(id));
             
 
              model.addAttribute("rentalManageList",rentalManage);
              
              rentalManageDto.setId(rentalManage.getId());
              rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
              rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
              rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
              rentalManageDto.setStatus(rentalManage.getStatus());
              rentalManageDto.setStockId(rentalManage.getStock().getId());
 
              model.addAttribute("rentalManageDto", rentalManageDto);
 
           }
   
           return "rental/edit";
       }
 
       @PostMapping("/rental/{id}/edit")
       public String update(@PathVariable("id") String id, Model model, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) {
           try {
            Date expectedRentalOn = rentalManageDto.getExpectedRentalOn();
            Date expectedReturnOn = rentalManageDto.getExpectedReturnOn();
               
            Optional<String> dayError = rentalManageDto.ValidDateTime(expectedRentalOn,expectedReturnOn);
            if(dayError.isPresent()){
                FieldError fieldError = new FieldError("rentalManageDto","expectedReturnOn", dayError.get());
                //dateErrorから取得したエラーメッセージをfieldErrorに入れる
                result.addError(fieldError);
                //resultにエラーの情報を入れる
                throw new Exception("Validation error");
                //エラーを投げる
            }

            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }

            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
            Optional <String> statusError = rentalManageDto.isValidStatus(rentalManage.getStatus());

            if (statusError.isPresent()) { 
                FieldError fieldError = new FieldError("rentalManageDto", "status", statusError.get());

                result.addError(fieldError);
 
                throw new Exception("Validation error");
            }

            //貸出可否チェックスタート
            //今回借りたい本の情報を取得
            Stock stock= this.stockService.findById(rentalManageDto.getStockId());
            //借りたい本が利用可かどうか調べる
            if (stock == null){
            //Stockがnullなら画面に飛ばす
                throw new Exception("Validation error");    
            }
            //Stockのステータスが貸出不可なら画面に飛ばす
            if (stock.getStatus() == 1){
                FieldError fieldError = new FieldError("rentalManageDto", "status", "この本は貸出できません");

                result.addError(fieldError);
 
                throw new Exception("Validation error");
            }
     
            String newStockId = rentalManageDto.getStockId();
            List<RentalManage> rentalManageList = this.rentalManageService.findByStockIdAndStatus(newStockId);
        
            if(rentalManageList == null){
                this.rentalManageService.save(rentalManageDto);
            }

            for(RentalManage list : rentalManageList){
                //リストの貸出管理番号とDtoの貸出管理番号が同じだった場合スキップ
                if(list.getId() == rentalManageDto.getId()){
                    continue;    
                }
                if(list.getExpectedRentalOn().compareTo(rentalManageDto.getExpectedReturnOn()) <= 0 &&
                rentalManageDto.getExpectedRentalOn().compareTo(list.getExpectedReturnOn()) <= 0){
                    FieldError fieldError = new FieldError("rentalManageDto", "status", "この期間では登録できません");
                    result.addError(fieldError);

                    throw new Exception("Validation error.");
                }
            }    
            /// 貸出可否チェック終了

            //更新
            rentalManageService.update(Long.valueOf(id),rentalManageDto);

            return "redirect:/rental/index";
              
           } catch (Exception e) {
               log.error(e.getMessage());
   
               ra.addFlashAttribute("rentalManageDto", rentalManageDto);
               ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);             
   
               return "redirect:/rental/{id}/edit";
           }
       }
    }
 