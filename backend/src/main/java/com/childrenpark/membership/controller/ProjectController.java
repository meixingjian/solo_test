package com.childrenpark.membership.controller;

import com.childrenpark.membership.common.Result;
import com.childrenpark.membership.context.UserContext;
import com.childrenpark.membership.entity.Project;
import com.childrenpark.membership.service.ProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(tags = "项目控制器")
@RestController
@RequestMapping("/admin/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @ApiOperation("获取所有项目列表")
    @GetMapping
    public Result<List<Project>> getProjects() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        List<Project> projects;
        if (UserContext.isAdmin()) {
            projects = projectService.getAllProjects();
        } else {
            projects = projectService.getActiveProjects();
        }
        
        return Result.success(projects);
    }

    @ApiOperation("获取项目详情")
    @GetMapping("/{id}")
    public Result<Project> getProject(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        Project project = projectService.getById(id);
        if (project == null || project.getDeleted() == 1) {
            return Result.error("项目不存在");
        }
        
        return Result.success(project);
    }

    @ApiOperation("添加项目")
    @PostMapping
    public Result<Project> addProject(@RequestBody Project project) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        if (!UserContext.isAdmin()) {
            return Result.forbidden();
        }
        
        if (project.getName() == null || project.getName().isEmpty()) {
            return Result.error("项目名称不能为空");
        }
        
        if (project.getPrice() == null || project.getPrice() <= 0) {
            return Result.error("扣币价格必须大于0");
        }
        
        try {
            Project saved = projectService.addProject(project);
            return Result.success(saved);
        } catch (Exception e) {
            log.error("添加项目失败", e);
            return Result.error("添加项目失败：" + e.getMessage());
        }
    }

    @ApiOperation("更新项目")
    @PutMapping("/{id}")
    public Result<Project> updateProject(@PathVariable Long id, @RequestBody Project project) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        if (!UserContext.isAdmin()) {
            return Result.forbidden();
        }
        
        try {
            Project updated = projectService.updateProject(id, project);
            return Result.success(updated);
        } catch (Exception e) {
            log.error("更新项目失败", e);
            return Result.error("更新项目失败：" + e.getMessage());
        }
    }

    @ApiOperation("删除项目")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        if (!UserContext.isAdmin()) {
            return Result.forbidden();
        }
        
        try {
            projectService.deleteProject(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除项目失败", e);
            return Result.error("删除项目失败：" + e.getMessage());
        }
    }

    @ApiOperation("切换项目状态")
    @PostMapping("/{id}/toggle-status")
    public Result<Project> toggleProjectStatus(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        
        if (!UserContext.isAdmin()) {
            return Result.forbidden();
        }
        
        try {
            Project project = projectService.toggleStatus(id);
            return Result.success(project);
        } catch (Exception e) {
            log.error("切换项目状态失败", e);
            return Result.error("切换项目状态失败：" + e.getMessage());
        }
    }
}
