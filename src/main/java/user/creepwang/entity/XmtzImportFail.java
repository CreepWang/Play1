package user.creepwang.entity;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导入时候错误信息
 *
 * @author CreepWang
 * @date 2022-07-04 11:17:09
 */
@Data


public class XmtzImportFail  {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */

    private String id;
    /**
     * 批次id
     */


    private Long picId;

    private String filePath;
    /**
     * 失败行号
     */

    private Integer rowNum;
    /**
     * 失败原因
     */

    private String failRemark;
    /**
     * 创建时间
     */

    private LocalDateTime createTime;


    private String pageNo;


}
