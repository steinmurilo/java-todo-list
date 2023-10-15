package br.com.mrstein.javatodolist.task;

import br.com.mrstein.javatodolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private final ITaskRepository taskRepository;

    public TaskController(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel task, HttpServletRequest request) {
        Object idUser = request.getAttribute("idUser");
        task.setIdUser((UUID) idUser);

        LocalDateTime currentDate = LocalDateTime.now();
        if (currentDate.isAfter(task.getStartAt()) || currentDate.isAfter(task.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Start date / end date must be greater than current date");
        }
        if (task.getStartAt().isAfter(task.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("End date must be greater than start date");
        }


        return ResponseEntity.status(HttpStatus.OK).body(taskRepository.save(task));
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        Object idUser = request.getAttribute("idUser");
        return taskRepository.findByIdUser((UUID) idUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel task, @PathVariable UUID id, HttpServletRequest request) {

        TaskModel taskFound = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task to update not found"));

        Object idUser = request.getAttribute("idUser");
        if(!taskFound.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User don't have permission to update this task");
        }

        Utils.copyNonNullProperties(task, taskFound);

        return ResponseEntity.status(HttpStatus.OK).body(taskRepository.save(taskFound));
    }
}
