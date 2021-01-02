package com.ooad.good.model.vo.flashSale;

import cn.edu.xmu.oomall.other.model.TimeDTO;
import com.ooad.good.model.po.FlashSalePo;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Future;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class FlashsaleNewRetVo {
    private Long id;
    @DateTimeFormat
    @Future
    private LocalDateTime flashDate;
    private TimeDTO timeDTO;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    public FlashsaleNewRetVo(FlashSalePo po)
    {
        this.flashDate = po.getFlashDate();
        this.id = po.getId();
        this.gmtCreate = po.getGmtCreate();
        this.gmtModified = po.getGmtModified();
    }

}
