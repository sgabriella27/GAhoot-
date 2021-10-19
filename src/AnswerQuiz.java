import java.io.Serializable;

public class AnswerQuiz implements Serializable {
	
	private int index;
	private String answer;
	private String username;

	public AnswerQuiz(int index, String answer, String username) {
		super();
		this.index = index;
		this.answer = answer;
		this.username = username;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
