package com.example.application.view;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.application.model_stuff.Todo;
import com.example.application.model_stuff.TodoRepo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("TODO LIST")
@Route("")
public class TodoView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private TodoRepo todoRepo;

    @Autowired
    public TodoView(TodoRepo todoRepo) {
        setSpacing(true);
        this.todoRepo = todoRepo;

        var task = new TextField();
        var button = new Button("Add");
        var todoList = new VerticalLayout();
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        task.setErrorMessage("Task cannot be empty");

        button.addClickListener(click -> {
            if (task.getValue().trim().isEmpty()) {
                task.setInvalid(true);
            } else {
                task.setInvalid(false);
                var todo = todoRepo.save(new Todo(task.getValue()));
                todoList.add(createCheckBox(todo, todoList));
                task.clear();
            }
        });
        
        task.addKeyPressListener(Key.ENTER, event -> {
            if (task.getValue().trim().isEmpty()) {
                task.setInvalid(true);
            } else {
                task.setInvalid(false);
                var todo = todoRepo.save(new Todo(task.getValue()));
                todoList.add(createCheckBox(todo, todoList));
                task.clear();
            }
        });
        
        todoRepo.findAll().forEach(todo -> {
            todoList.add(createCheckBox(todo, todoList));
        });
        
        H2 author = new H2("Made By Yuri Casadei");
        author.getStyle().set("font-size", "16px"); 
        author.getStyle().set("margin-left", "1730px");
        
        H2 h2 = new H2("With MySQL");
        h2.getStyle().set("font-size", "14px"); 

        add(
        	author,
            new H1("TODO LIST"),
            h2,
            new HorizontalLayout(task, button),
            todoList
        );

        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "left-center");
    }

    private Component createCheckBox(Todo todo, VerticalLayout todoList) {
        Checkbox checkbox = new Checkbox(todo.getTask(), todo.isDone());
        checkbox.getStyle().set("font-size", "20px");
        checkbox.getStyle().set("margin-left", "500px");

        checkbox.addValueChangeListener(event -> {
            if (event.getValue()) {
                getUI().ifPresent(ui -> ui.access(() -> {
                    ui.setPollInterval(100);
                    ui.addPollListener(pollEvent -> {
                        checkbox.setEnabled(false);
                        ui.setPollInterval(500);
                        ui.addPollListener(pollEvent2 -> {
                            todoRepo.delete(todo);
                            todoList.remove(checkbox);
                            ui.setPollInterval(-1);
                        });
                    });
                }));
            } else {
                todo.setDone(event.getValue());
                todoRepo.save(todo);
            }
        });
        return checkbox;
    }
}
