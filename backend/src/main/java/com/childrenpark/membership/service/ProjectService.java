package com.childrenpark.membership.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.childrenpark.membership.entity.Project;

import java.util.List;

public interface ProjectService extends IService<Project> {

    List<Project> getActiveProjects();

    List<Project> getAllProjects();

    Project addProject(Project project);

    Project updateProject(Long id, Project project);

    void deleteProject(Long id);

    Project toggleStatus(Long id);
}
