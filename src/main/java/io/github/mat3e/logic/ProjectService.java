package io.github.mat3e.logic;

import io.github.mat3e.TaskConfigurationProperties;
import io.github.mat3e.controller.IllegalExceptionProcessing;
import io.github.mat3e.model.*;
import io.github.mat3e.model.projection.GroupReadModel;
import io.github.mat3e.model.projection.GroupTaskWriteModel;
import io.github.mat3e.model.projection.GroupWriteModel;
import io.github.mat3e.model.projection.ProjectWriteModel;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@IllegalExceptionProcessing
public class ProjectService {
    private ProjectRepository repository;
    private TaskGroupRepository taskGroupRepository;
    private TaskGroupService taskGroupservice;
    private TaskConfigurationProperties config;

    public ProjectService(final ProjectRepository repository,
                          final TaskGroupRepository taskGroupRepository,
                          final TaskGroupService taskGroupservice, final TaskConfigurationProperties config) {

        this.repository = repository;
        this.taskGroupRepository = taskGroupRepository;
        this.taskGroupservice = taskGroupservice;
        this.config = config;

    }

    public List<Project> readAll(){
        return repository.findAll();
    }

    public Project save(final ProjectWriteModel toSave){
        return repository.save(toSave.toProject());
    }

    public GroupReadModel createGroup(@NotBlank @Valid LocalDateTime deadline, int projectId) {
        if (!config.getTemplate().isAllowMultipleTasks() && taskGroupRepository.existsByDoneIsFalseAndProject_Id(projectId)) {
            throw new IllegalStateException("Only one undone group from project is allowed");
        }
        return repository.findById(projectId)
                .map(project -> {
                    var targetGroup = new GroupWriteModel();
                    targetGroup.setDescription(project.getDescription());
                    targetGroup.setTasks(
                            project.getSteps().stream()
                                    .map(projectStep -> {
                                                var task = new GroupTaskWriteModel();
                                                task.setDescription(projectStep.getDescription());
                                                task.setDeadline(deadline.plusDays(projectStep.getDaysToDeadline()));
                                                return task;
                                            }
                                    ).collect(Collectors.toList())
                    );
                    return taskGroupservice.createGroup(targetGroup, project);

                }).orElseThrow(() -> new IllegalArgumentException("Project with given id not found"));
    }

}
