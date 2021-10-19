import java.io.Serializable;

public class CreateQuiz implements Serializable {
	
	private String theme;
	private String question;
	private String answer;
	private String username;

	public CreateQuiz(String theme, String question, String answer, String username) {
		super();
		this.theme = theme;
		this.question = question;
		this.answer = answer;
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	@Override
	public String toString() {
		return "CreateQuiz [theme=" + theme + ", question=" + question + ", answer=" + answer + ", username=" + username
				+ "]";
	}
}
