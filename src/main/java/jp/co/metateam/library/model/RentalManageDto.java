package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;

import jp.co.metateam.library.values.RentalStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Getter;
import lombok.Setter;

/**
 * 貸出管理DTO
 */
@Getter
@Setter
public class RentalManageDto {

    private Long id;

    @NotEmpty(message="在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message="社員番号は必須です")
    private String employeeId;

    @NotNull(message="貸出ステータスは必須です")
    private Integer status;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="貸出予定日は必須です")
    @FutureOrPresent(message="貸出予定日は現在より後の日にちを選択してください")
    private Date expectedRentalOn;


    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="返却予定日は必須です")
    @FutureOrPresent(message="返却予定日は現在より後の日にちを選択してください")
    private Date expectedReturnOn;

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;

    public Optional <String>  isvalidStatus(Integer preStatus){

        if (preStatus.equals (RentalStatus.RENT_WAIT.getValue())) {
           if (this.status.equals (RentalStatus.RETURNED.getValue())){
             return Optional.of ("貸出ステータスは貸出待ちから返却済みに選択できません");
            }

        } else if (preStatus.equals (RentalStatus.RENTAlING.getValue())) {
            if (this.status.equals (RentalStatus.RENT_WAIT.getValue())) {
             return Optional.of ("貸出ステータスは貸出中から貸出待ちに選択できません");   
             } else if (this.status.equals (RentalStatus.CANCELED.getValue())) {
             return Optional.of ("貸出ステータスは貸出中からキャンセルに選択できません");
             }

        } else if (preStatus.equals (RentalStatus.RETURNED.getValue())) {
            if (this.status.equals (RentalStatus.RENT_WAIT.getValue())) {
             return Optional.of ("貸出ステータスは返却済みから貸出待ちに選択できません");   
             } else if (this.status.equals(RentalStatus.CANCELED.getValue())) {
             return Optional.of ("貸出ステータスは返却済みから貸出中に選択できません");
             } else if (this.status.equals(RentalStatus.CANCELED.getValue())) {
             return Optional.of ("貸出ステータスは返却済みからキャンセルに選択できません");
             }
            }

        else if (preStatus.equals (RentalStatus.CANCELED.getValue())) {
            if (this.status.equals (RentalStatus.RENT_WAIT.getValue())) {
             return Optional.of ("貸出ステータスはキャンセルから貸出待ちに選択できません");   
             } else if (this.status.equals(RentalStatus.RENTAlING.getValue())) {
             return Optional.of ("貸出ステータスはキャンセルから貸出中に選択できません");
             } else if (this.status.equals(RentalStatus.RETURNED.getValue())) {
             return Optional.of ("貸出ステータスはキャンセルから返却済みに選択できません");
             }
            }   
        
         {
            return Optional.empty();
    }
}
}
