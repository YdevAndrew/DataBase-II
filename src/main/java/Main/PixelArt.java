package Main;

public class PixelArt {
    private String name;
    private boolean isCompleted;
    private String status;  // Novo campo para armazenar o status (To-Do, In Progress, Done)

    public PixelArt(String name, boolean isCompleted, String status) {
        this.name = name;
        this.isCompleted = isCompleted;
        this.status = status;
    }

    // Getters e setters para status
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
}
