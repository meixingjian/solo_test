package com.childrenpark.membership.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.childrenpark.membership.entity.Project;
import com.childrenpark.membership.mapper.ProjectMapper;
import com.childrenpark.membership.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Override
    public List<Project> getActiveProjects() {
        return list(new LambdaQueryWrapper<Project>()
                .eq(Project::getStatus, 1)
                .eq(Project::getDeleted, 0)
                .orderByAsc(Project::getSort)
                .orderByDesc(Project::getCreateTime));
    }

    @Override
    public List<Project> getAllProjects() {
        return list(new LambdaQueryWrapper<Project>()
                .eq(Project::getDeleted, 0)
                .orderByAsc(Project::getSort)
                .orderByDesc(Project::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project addProject(Project project) {
        project.setStatus(project.getStatus() != null ? project.getStatus() : 1);
        project.setSort(project.getSort() != null ? project.getSort() : 0);
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        project.setDeleted(0);
        save(project);
        return project;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project updateProject(Long id, Project project) {
        Project existing = getById(id);
        if (existing == null) {
            throw new RuntimeException("项目不存在");
        }
        
        if (project.getName() != null) {
            existing.setName(project.getName());
        }
        if (project.getDescription() != null) {
            existing.setDescription(project.getDescription());
        }
        if (project.getPrice() != null) {
            existing.setPrice(project.getPrice());
        }
        if (project.getMaxCapacity() != null) {
            existing.setMaxCapacity(project.getMaxCapacity());
        }
        if (project.getStatus() != null) {
            existing.setStatus(project.getStatus());
        }
        if (project.getIcon() != null) {
            existing.setIcon(project.getIcon());
        }
        if (project.getSort() != null) {
            existing.setSort(project.getSort());
        }
        existing.setUpdateTime(LocalDateTime.now());
        
        updateById(existing);
        return existing;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        project.setDeleted(1);
        project.setUpdateTime(LocalDateTime.now());
        updateById(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project toggleStatus(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        project.setStatus(project.getStatus() == 1 ? 0 : 1);
        project.setUpdateTime(LocalDateTime.now());
        updateById(project);
        return project;
    }
}
