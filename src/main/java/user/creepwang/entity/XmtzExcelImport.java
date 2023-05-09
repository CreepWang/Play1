package user.creepwang.entity;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * Excel导入日志
 *
 * @author CreepWang
 * @date 2022-07-07 10:57:57
 */
@Data

public class XmtzExcelImport {

    /**
     * 主键id
     */

    private String id;

    private String azc005;
    /**
     * 导入类型
     * 01   单位台账
     * 02   涉密网台账
     * 03   资产-终端
     * 04  资产-外设
     * 05  资产-服务器
     * 06  资产-自建系统
     * 07  资产-使用系统
     */

    private String importType;
    /**
     * 导入开始时间
     */

    private LocalDateTime importStartTime;
    /**
     * 导入结束时间
     */

    private LocalDateTime importEndTime;
    /**
     * 导入状态
     * 3 成功
     * 2 取消
     * -1 失败
     */

    private String importState;
    /**
     * 创建时间
     */

    private LocalDateTime createTime;
    /**
     * 导入附件路径
     */

    private String filePath;
    /**
     * 导入原表
     */

    private String fileName;
    /**
     * 导入目标表
     */

    private String fileId;
    /**
     * 总记录数
     */

    private Integer totalNum;
    /**
     * 成功条数
     */

    private Integer sucessNum;
    /**
     * 失败条数
     */

    private Integer failueNum;
    /**
     * 操作人
     */

    private Long operUser;


    private Long unitId;


    private Long selectUnitid;


}


