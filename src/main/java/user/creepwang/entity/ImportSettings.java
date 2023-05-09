package user.creepwang.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Excel配置
 *
 * @author CreepWang
 * @date 2022-07-04 16:20:58
 */
@Data
public class ImportSettings{


    private String id;

    private String attrId;

    private String cellName;

    private String cellIdentificationof;

    private Integer cellIndex;

    private String cellRule;

    private String codeName;

    private String pageNo;

    private List<SysDict> dictList;


   }




