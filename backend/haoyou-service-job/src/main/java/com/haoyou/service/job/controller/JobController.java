package com.haoyou.service.job.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haoyou.common.BusinessException;
import com.haoyou.common.Result;
import com.haoyou.service.job.entity.JobCognition;
import com.haoyou.service.job.mapper.JobCognitionMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 读者端岗位认知接口（走网关 /api/job/** 路由，直连 8085 亦可）
 */
@RestController
@RequestMapping("/api/job")
public class JobController {

    private final JobCognitionMapper jobMapper;

    public JobController(JobCognitionMapper jobMapper) {
        this.jobMapper = jobMapper;
    }

    /** 岗位列表：读者端默认只看启用；管理端传 all=true 看全部 */
    @GetMapping("/list")
    public Result<List<JobCognition>> list(@RequestParam(defaultValue = "false") boolean all) {
        LambdaQueryWrapper<JobCognition> wrapper = new LambdaQueryWrapper<>();
        if (!all) {
            wrapper.eq(JobCognition::getStatus, 1);
        }
        wrapper.orderByAsc(JobCognition::getId);
        return Result.success(jobMapper.selectList(wrapper));
    }

    /** 岗位认知详情 */
    @GetMapping("/{id}")
    public Result<JobCognition> detail(@PathVariable Long id) {
        JobCognition job = jobMapper.selectById(id);
        if (job == null || (job.getStatus() != null && job.getStatus() == 0)) {
            throw new BusinessException(404, "岗位不存在或已下架");
        }
        return Result.success(job);
    }

    // ---------- 管理端：岗位管理（FR-24~27） ----------

    /** 创建岗位（含停用状态的管理列表） */
    @PostMapping
    public Result<Long> create(@RequestBody JobCognition job) {
        if (job.getJobName() == null || job.getJobName().isBlank()) {
            throw new BusinessException(400, "岗位名称不能为空");
        }
        job.setId(null);
        if (job.getStatus() == null) {
            job.setStatus(1);
        }
        jobMapper.insert(job);
        return Result.success(job.getId());
    }

    /** 编辑岗位 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody JobCognition job) {
        JobCognition db = jobMapper.selectById(id);
        if (db == null) {
            throw new BusinessException(404, "岗位不存在");
        }
        job.setId(id);
        jobMapper.updateById(job);
        return Result.success();
    }

    /** 删除岗位（逻辑删） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        jobMapper.deleteById(id);
        return Result.success();
    }
}
