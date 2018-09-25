package il.ac.tau.cs.sw1.trivia;

import java.util.ArrayList;
import java.util.List;

public class Question {
	private String questionBody;
	private List<String> answers;
	private String correctAnswer;
	private boolean seenFlag;
	
	public Question(String qBody, String a1,String a2, String a3, String a4) {
		this.questionBody = qBody;
		this.correctAnswer = a1;
		this.answers = new ArrayList<String>();
		this.answers.add(a1);
		this.answers.add(a2);
		this.answers.add(a3);
		this.answers.add(a4);
	}
	
	public List<String> getAnswers(){
		return this.answers;
	}
	
	public String getCorrectAnswer() {
		return this.correctAnswer;
	}
	
	public String getQuestionBody() {
		return this.questionBody;
	}
	
	public boolean hasRead() {
		return this.seenFlag;
	}
	
	public void setHasRead(boolean flag) {
		this.seenFlag = flag;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((answers == null) ? 0 : answers.hashCode());
		result = prime * result + ((questionBody == null) ? 0 : questionBody.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Question other = (Question) obj;
		if (answers == null) {
			if (other.answers != null)
				return false;
		} else if (!answers.equals(other.answers))
			return false;
		if (questionBody == null) {
			if (other.questionBody != null)
				return false;
		} else if (!questionBody.equals(other.questionBody))
			return false;
		return true;
	}
}
