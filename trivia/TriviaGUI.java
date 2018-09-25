package il.ac.tau.cs.sw1.trivia;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TriviaGUI {

	private static final int MAX_ERRORS = 3;
	private static final int ANSWERS_NUM = 4;
	private Shell shell;
	private Label scoreLabel;
	private Composite questionPanel;
	private Label startupMessageLabel;
	private Font boldFont;
	private String lastAnswer;
	
	
	private Set<Question> questionsRep;
	private int currentQuestion;
	private int totalScore;
	private int errorInArowCount;
	private int questionsCount;
	private boolean passQuestionUsed;
	private boolean fiftyFiftyUsed;
	private boolean gameOver;
	
	// Currently visible UI elements.
	Label instructionLabel;
	Label questionLabel;
	private List<Button> answerButtons = new LinkedList<>();
	private Button passButton;
	private Button fiftyFiftyButton;

	public void open() {
		createShell();
		runApplication();
	}

	/**
	 * Creates the widgets of the application main window
	 */
	private void createShell() {
		Display display = Display.getDefault();
		shell = new Shell(display);
		shell.setText("Trivia");

		// window style
		Rectangle monitor_bounds = shell.getMonitor().getBounds();
		shell.setSize(new Point(monitor_bounds.width / 3,
				monitor_bounds.height / 4));
		shell.setLayout(new GridLayout());

		FontData fontData = new FontData();
		fontData.setStyle(SWT.BOLD);
		boldFont = new Font(shell.getDisplay(), fontData);

		// create window panels
		createFileLoadingPanel();
		createScorePanel();
		createQuestionPanel();
	}

	/**
	 * Creates the widgets of the form for trivia file selection
	 */
	private void createFileLoadingPanel() {
		final Composite fileSelection = new Composite(shell, SWT.NULL);
		fileSelection.setLayoutData(GUIUtils.createFillGridData(1));
		fileSelection.setLayout(new GridLayout(4, false));

		final Label label = new Label(fileSelection, SWT.NONE);
		label.setText("Enter trivia file path: ");

		// text field to enter the file path
		final Text filePathField = new Text(fileSelection, SWT.SINGLE
				| SWT.BORDER);
		filePathField.setLayoutData(GUIUtils.createFillGridData(1));

		// "Browse" button
		final Button browseButton = new Button(fileSelection, SWT.PUSH);
		browseButton.setText("Browse");
		
		// listener from browse button
		browseButton.addSelectionListener(new SelectionListener() {
			@Override public void widgetSelected(SelectionEvent e) {
				String fileTxt = GUIUtils.getFilePathFromFileDialog(shell);
				if(fileTxt!=null)
					filePathField.setText(fileTxt);
			}
			@Override public void widgetDefaultSelected(SelectionEvent e) {}
		});

		// "Play!" button
		final Button playButton = new Button(fileSelection, SWT.PUSH);
		playButton.setText("Play!");
		lastAnswer="";	
		playButton.addSelectionListener(new SelectionListener() {
			@Override public void widgetSelected(SelectionEvent e) {				
				try {
					if(filePathField.getText()!="" && filePathField.getText()!=null) {
						gameOver = false;			
						errorInArowCount=0;
						questionsCount=0;
						passQuestionUsed = false;
						fiftyFiftyUsed = false;
						totalScore=0;
						updateScore(0);
						loadQuestions(filePathField.getText());
						fetchQuestion();

					}
				} catch (IOException e1) {
					GUIUtils.showErrorDialog(shell, "Trivia file format error: Trivia file row must containing a question and four answers, separated by tabs.");
				}
				catch (Exception e2) {
					GUIUtils.showErrorDialog(shell, "Trivia file format error: Trivia file row must containing a question and four answers, separated by tabs.");
				}
				
			}
			@Override public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}

	/**
	 * Creates the panel that displays the current score
	 */
	private void createScorePanel() {
		Composite scorePanel = new Composite(shell, SWT.BORDER);
		scorePanel.setLayoutData(GUIUtils.createFillGridData(1));
		scorePanel.setLayout(new GridLayout(2, false));

		final Label label = new Label(scorePanel, SWT.NONE);
		label.setText("Total score: ");

		// The label which displays the score; initially empty
		scoreLabel = new Label(scorePanel, SWT.NONE);
		scoreLabel.setLayoutData(GUIUtils.createFillGridData(1));
	}

	/**
	 * Creates the panel that displays the questions, as soon as the game
	 * starts. See the updateQuestionPanel for creating the question and answer
	 * buttons
	 */
	private void createQuestionPanel() {
		questionPanel = new Composite(shell, SWT.BORDER);
		questionPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, true));
		questionPanel.setLayout(new GridLayout(2, true));

		// Initially, only displays a message
		startupMessageLabel = new Label(questionPanel, SWT.NONE);
		startupMessageLabel.setText("No question to display, yet.");
		startupMessageLabel.setLayoutData(GUIUtils.createFillGridData(2));
	}

	/**
	 * Serves to display the question and answer buttons
	 */
	private void updateQuestionPanel(String question, List<String> answers) {
		// Save current list of answers.
		List<String> currentAnswers = answers;
		
		// clear the question panel
		Control[] children = questionPanel.getChildren();
		for (Control control : children) {
			control.dispose();
		}

		// create the instruction label
		instructionLabel = new Label(questionPanel, SWT.CENTER | SWT.WRAP);
		instructionLabel.setText(lastAnswer + "Answer the following question:");
		instructionLabel.setLayoutData(GUIUtils.createFillGridData(2));

		// create the question label
		questionLabel = new Label(questionPanel, SWT.CENTER | SWT.WRAP);
		questionLabel.setText(question);
		questionLabel.setFont(boldFont);
		questionLabel.setLayoutData(GUIUtils.createFillGridData(2));

		// create the answer buttons
		for (int i = 0; i < 4; i++) {
			Button answerButton = new Button(questionPanel, SWT.PUSH | SWT.WRAP);
			answerButton.setText(currentAnswers.get(i));
			GridData answerLayoutData = GUIUtils.createFillGridData(1);
			answerLayoutData.verticalAlignment = SWT.FILL;
			answerButton.setLayoutData(answerLayoutData);
			answerButton.addSelectionListener(new AnswersListener());
			answerButtons.add(answerButton);
		}

		// create the "Pass" button to skip a question
		passButton = new Button(questionPanel, SWT.PUSH);
		passButton.setText("Pass");
		GridData data = new GridData(GridData.END, GridData.CENTER, true,
				false);
		data.horizontalSpan = 1;
		passButton.setLayoutData(data);
		
		//listener to the pass button
		passButton.addSelectionListener(new HelpersListener());
		
		// create the "50-50" button to show fewer answer options
		fiftyFiftyButton = new Button(questionPanel, SWT.PUSH);
		fiftyFiftyButton.setText("50-50");
		data = new GridData(GridData.BEGINNING, GridData.CENTER, true,
				false);
		data.horizontalSpan = 1;
		fiftyFiftyButton.setLayoutData(data);
		fiftyFiftyButton.addSelectionListener(new HelpersListener());

		// two operations to make the new widgets display properly
		questionPanel.pack();
		questionPanel.getParent().layout();
	}

	/**
	 * Opens the main window and executes the event loop of the application
	 */
	private void runApplication() {
		shell.open();
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		boldFont.dispose();
	}
	
	private void loadQuestions(String filePath) throws IOException {
		questionsRep = new HashSet<Question>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			String[] splittedLine = line.split("\t");
			questionsRep.add(new Question(splittedLine[0],splittedLine[1],splittedLine[2],splittedLine[3],splittedLine[4]));
		}
		bufferedReader.close();
		System.out.println("Num of questions: " + questionsRep.size());
	}
	
	private void updateScore(int delta) {
		totalScore +=delta;
		scoreLabel.setText(Integer.toString(totalScore));
	}
	
	private void updateButtons() {
		if(fiftyFiftyUsed) {
			if(totalScore<=0) {
				fiftyFiftyButton.setEnabled(false);
			}
			else {
				fiftyFiftyButton.setEnabled(true);
			}
		}
		else {
			fiftyFiftyButton.setEnabled(true);
		}
		
		if(passQuestionUsed) {
			if(totalScore<=0) {
				passButton.setEnabled(false);
			}
			else {
				passButton.setEnabled(true);
			}
		}
		else {
			passButton.setEnabled(true);
		}
	}
	
	private void fetchQuestion() {
		Random rnd = new Random();
		int qNum = rnd.nextInt(questionsRep.size());
		while(((Question)(questionsRep.toArray()[qNum])).hasRead()) {
			qNum = rnd.nextInt(questionsRep.size());
		}
		List<String> displayedAnswers = ((Question)(questionsRep.toArray()[qNum])).getAnswers();
		Collections.shuffle(displayedAnswers);
		updateQuestionPanel(((Question)(questionsRep.toArray()[qNum])).getQuestionBody(),displayedAnswers);
		
		((Question)(questionsRep.toArray()[qNum])).setHasRead(true);
		currentQuestion = qNum;
		questionsCount++;
	}
	
	private void gameOver() {
		GUIUtils.showInfoDialog(shell, "GAME OVER", "Your final score is " + totalScore + " after " + questionsCount + " questions");
	}
	
	public class HelpersListener implements SelectionListener{

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource() instanceof Button) {
				Button b = (Button) e.getSource();
				if(!gameOver){
					//50-50 selection
					if(b.getText().equals("50-50")) {
						if(fiftyFiftyUsed) {
							if(totalScore>0) {
								updateScore(-1);
								elliminateAnswers();
							}
						}
						else {
							fiftyFiftyUsed = true;
							elliminateAnswers();
						}
						//set enable false anyway
						fiftyFiftyButton.setEnabled(false);
					}
					if(b.getText().equals("Pass")) {
						if(passQuestionUsed) {
							if(totalScore>0) {
								updateScore(-1);
								fetchQuestion();
							}
						}
						else {
							passQuestionUsed = true;
							fetchQuestion();
						}
					}
					updateButtons();
				}
				else {
					gameOver();
				}
			}
		}
		
		private void elliminateAnswers() {
			int elliminatedAns=0;
			int buttonArrayLength = answerButtons.size()-1;
			Random rnd = new Random();
			while(elliminatedAns<2) {
				int aNum = rnd.nextInt(ANSWERS_NUM);
				if(!answerButtons.get(buttonArrayLength-aNum).getText().equals(((Question)(questionsRep.toArray()[currentQuestion])).getCorrectAnswer()) && answerButtons.get(buttonArrayLength-aNum).getEnabled()) {
					answerButtons.get(buttonArrayLength-aNum).setEnabled(false);
					elliminatedAns++;
				}
			}
		}
	}
	
	public class AnswersListener implements SelectionListener{
		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {			
		}
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource() instanceof Button && !gameOver) {
				Button b = (Button) e.getSource();
				//if we answered correctly
				if(b.getText().equals(((Question)(questionsRep.toArray()[currentQuestion])).getCorrectAnswer())) {
					errorInArowCount=0;
					updateScore(3);
					lastAnswer = "Correct! ";
				}
				//wrong answer
				else {
					errorInArowCount++;
					updateScore(-2);
					lastAnswer = "Wrong... ";
				}
				//handle end of game
				if(this.endOfGame()) {
					gameOver = true;
					gameOver();
				}
				else {
					fetchQuestion();
				}
			}
			//update helpers anyway
			updateButtons();
		}
		
		private boolean endOfGame() {
			return (questionsCount==questionsRep.size() || errorInArowCount==MAX_ERRORS);
		}
		
	}
}

