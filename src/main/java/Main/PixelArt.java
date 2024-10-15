package Main;

import java.time.LocalDate;

public class PixelArt {
    private String name;
    private boolean isCompleted;
    private String status;  // Novo campo para armazenar o status (To-Do, In Progress, Done)
    private LocalDate startDate;  // Data de início
    private LocalDate endDate;    // Data de término

    // Construtor atualizado com datas
    public PixelArt(String name, String status, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.isCompleted = false;  // Inicialmente não concluída
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters e setters para status, nome, data de início e término
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
