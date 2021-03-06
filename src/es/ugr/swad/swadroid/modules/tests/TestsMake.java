/*
 *  This file is part of SWADroid.
 *
 *  Copyright (C) 2010 Juan Miguel Boyero Corral <juanmi1982@gmail.com>
 *
 *  SWADroid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SWADroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with SWADroid.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.ugr.swad.swadroid.modules.tests;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.ksoap2.SoapFault;
import org.xmlpull.v1.XmlPullParserException;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import es.ugr.swad.swadroid.Global;
import es.ugr.swad.swadroid.Preferences;
import es.ugr.swad.swadroid.R;
import es.ugr.swad.swadroid.model.Course;
import es.ugr.swad.swadroid.model.Test;
import es.ugr.swad.swadroid.model.TestAnswer;
import es.ugr.swad.swadroid.model.TestQuestion;
import es.ugr.swad.swadroid.model.TestTag;
import es.ugr.swad.swadroid.modules.Module;
import es.ugr.swad.swadroid.widget.NumberPicker;
import es.ugr.swad.swadroid.widget.TextProgressBar;

/**
 * Tests module for evaluate user skills in a course
 * @author Juan Miguel Boyero Corral <juanmi1982@gmail.com>
 */
public class TestsMake extends Module {
	/**
	 * Test's number of questions
	 */
	private int numQuestions;
	/**
	 * Test data
	 */
	private Test test;
	/**
	 * Tags's list of the test
	 */
	private List<TestTag> tagsList;
	/**
	 * Answer types's list of the test
	 */
	private List<String> answerTypesList;
	/**
	 * Click listener for courses dialog cancel button
	 */
	private OnItemClickListener tagsAnswersTypeItemClickListener;
	/**
	 * Adapter for answer TF questions
	 */
	private ArrayAdapter<String> tfAdapter;
	/**
	 * Test question being showed
	 */
	private int actualQuestion;
	/**
	 * Tests tag name for Logcat
	 */
	public static final String TAG = Global.APP_TAG + " TestsMake";
	/**
	 * Application preferences.
	 */
	protected static Preferences prefs = new Preferences(); 

	/**
	 * Sets layout maintaining tests action bar
	 * @param layout Layout to be applied
	 */
	private void setLayout(int layout) {
		ImageView image;
		TextView text;

		setContentView(layout);

		image = (ImageView)this.findViewById(R.id.moduleIcon);
		image.setBackgroundResource(R.drawable.test);

		text = (TextView)this.findViewById(R.id.moduleName);
		text.setText(R.string.testsModuleLabel);
	}

	/**
	 * Screen to select the number of questions in the test 
	 */
	private void setNumQuestions() {
		final NumberPicker numberPicker;
		Button acceptButton;

		setLayout(R.layout.tests_num_questions);

		numberPicker = (NumberPicker)findViewById(R.id.testNumQuestionsNumberPicker);		
		numberPicker.setRange(test.getMin(), test.getMax());
		numberPicker.setCurrent(test.getDef());

		acceptButton = (Button)findViewById(R.id.testNumQuestionsAcceptButton);
		acceptButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				numQuestions = numberPicker.getCurrent();

				if(isDebuggable) {
					Log.d(TAG, "numQuestions="+numQuestions);
				}

				setTags();
			}
		});
	}

	/**
	 * Screen to select the tags that will be present in the test
	 */
	private void setTags() {
		Button acceptButton;
		final ListView checkBoxesList;
		final TagsArrayAdapter tagsAdapter;
		final List<TestTag> allTagsList = dbHelper.getOrderedCourseTags(Global.getSelectedCourseCode());

		//Add "All tags" item in list's top
		allTagsList.add(0, new TestTag(0, getResources().getString(R.string.allMsg), 0));

		setLayout(R.layout.tests_tags);

		checkBoxesList = (ListView) findViewById(R.id.testTagsList); 
		tagsAdapter = new TagsArrayAdapter(this, R.layout.list_item_multiple_choice, allTagsList);
		checkBoxesList.setAdapter(tagsAdapter);
		checkBoxesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		checkBoxesList.setOnItemClickListener(tagsAnswersTypeItemClickListener);

		acceptButton = (Button)findViewById(R.id.testTagsAcceptButton);
		acceptButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				int childsCount = checkBoxesList.getCount();
				SparseBooleanArray checkedItems = checkBoxesList.getCheckedItemPositions();
				tagsList = new ArrayList<TestTag>();

				//If "All tags" item checked, add the whole list to the list of selected tags
				if(checkedItems.get(0, false)) {
					tagsList.add(new TestTag(0, null, "all", 0));

					//If "All tags" item not checked, add the selected items to the list of selected tags
				} else {				
					for(int i=0; i<childsCount; i++) {
						if(checkedItems.get(i, false)) {
							tagsList.add(tagsAdapter.getItem(i));
						}
					}
				}

				if(isDebuggable) {
					Log.d(TAG, "tagsList="+tagsList.toString());
				}

				//If no tags selected, show a message to notice user
				if(tagsList.isEmpty()) {
					Toast.makeText(getBaseContext(), R.string.testNoTagsSelectedMsg, Toast.LENGTH_LONG).show();

					//If any tag is selected, show the answer types selection screen
				} else {
					setAnswerTypes();
				}
			}
		});
	}

	/**
	 * Screen to select the answer types that will be present in the test
	 */
	private void setAnswerTypes() {
		Button acceptButton;
		final ListView checkBoxesList;
		final AnswerTypesArrayAdapter answerTypesAdapter;

		setLayout(R.layout.tests_answer_types);

		checkBoxesList = (ListView) findViewById(R.id.testAnswerTypesList); 
		answerTypesAdapter = new AnswerTypesArrayAdapter(this, R.array.testAnswerTypes,
				R.array.testAnswerTypesNames, R.layout.list_item_multiple_choice);
		checkBoxesList.setAdapter(answerTypesAdapter);
		checkBoxesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		checkBoxesList.setOnItemClickListener(tagsAnswersTypeItemClickListener);

		acceptButton = (Button)findViewById(R.id.testAnswerTypesAcceptButton);
		acceptButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				int childsCount = checkBoxesList.getCount();
				SparseBooleanArray checkedItems = checkBoxesList.getCheckedItemPositions();
				answerTypesList = new ArrayList<String>();

				/*
				 * If "All tags" item checked, add the whole list to the list of selected answer types,
				 * else, add the selected items to the list of selected answer types
				 */
				if(checkedItems.get(0, false)) {
					answerTypesList.add("all");
				} else {
					for(int i=1; i<childsCount; i++) {
						if(checkedItems.get(i, false)) {
							answerTypesList.add((String) answerTypesAdapter.getItem(i));
						}
					}
				}

				if(isDebuggable) {
					Log.d(TAG, "answerTypesList="+answerTypesList.toString());
				}

				//If no answer types selected, show a message to notice user
				if(answerTypesList.isEmpty()) {
					Toast.makeText(getBaseContext(), R.string.testNoAnswerTypesSelectedMsg, Toast.LENGTH_LONG)
					.show();

					//If any answer type is selected, generate the test and show the first question screen
				} else {
					makeTest();
				}
			}
		});
	}

	/**
	 * Shows a test question on screen
	 * @param pos Question's position in questions's list of the test
	 */
	private void showQuestion(int pos) {
		TestQuestion question = test.getQuestions().get(pos);
		List<TestAnswer> answers = question.getAnswers();
		TestAnswer a;
		ListView testMakeList = (ListView) findViewById(R.id.testMakeList);
		TextView stem = (TextView) findViewById(R.id.testMakeText);
		TextView score = (TextView) findViewById(R.id.testMakeQuestionScore);
		TextView textCorrectAnswer = (TextView) findViewById(R.id.testMakeCorrectAnswer);
		EditText textAnswer = (EditText) findViewById(R.id.testMakeEditText);
		ImageView img = (ImageView) findViewById(R.id.testMakeCorrectAnswerImage);
		CheckedAnswersArrayAdapter checkedAnswersAdapter;
		String answerType = question.getAnswerType();
		String feedback = test.getFeedback();
		String correctAnswer = "";
		int numAnswers = answers.size();
		Float questionScore;
		DecimalFormat df = new DecimalFormat("0.00");

		score.setVisibility(View.GONE);
		textAnswer.setVisibility(View.GONE);
		textCorrectAnswer.setVisibility(View.GONE);
		testMakeList.setVisibility(View.GONE);
		img.setVisibility(View.GONE);

		stem.setText(Html.fromHtml(question.getStem()));
		if(answerType.equals("text")
				|| answerType.equals("int")
				|| answerType.equals("float")) {

			if(!answerType.equals("text")) {
				textAnswer.setRawInputType(InputType.TYPE_CLASS_NUMBER);
			} else {
				textAnswer.setRawInputType(InputType.TYPE_CLASS_TEXT);
			}

			a = answers.get(0);
			textAnswer.setText(a.getUserAnswer());
			textAnswer.setVisibility(View.VISIBLE);

			if(test.isEvaluated() && feedback.equals("eachGoodBad")) {
				if(answerType.equals("float")) {
					correctAnswer = "[" + a.getAnswer() + ";" + answers.get(1).getAnswer() + "]";
				} else {
					for(int i=0; i<numAnswers; i++) {
						a = answers.get(i);
						correctAnswer += a.getAnswer() + "<br/>";
					}
				}

				textCorrectAnswer.setText(Html.fromHtml(correctAnswer));
				textCorrectAnswer.setVisibility(View.VISIBLE);
			}
		} else if(answerType.equals("multipleChoice")) {
			checkedAnswersAdapter = new CheckedAnswersArrayAdapter(this, R.layout.list_item_multiple_choice,
					answers, test.isEvaluated(), test.getFeedback(), answerType);

			testMakeList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);			
			testMakeList.setAdapter(checkedAnswersAdapter);	

			for(int i=0; i<numAnswers; i++) {
				a = answers.get(i);
				testMakeList.setItemChecked(i, Global.parseStringBool(a.getUserAnswer()));
			}

			testMakeList.setVisibility(View.VISIBLE);
		} else {				
			if(answerType.equals("TF") && (numAnswers < 2)) {
				if(answers.get(0).getAnswer().equals("T")) {
					answers.add(1, new TestAnswer(0, 1, 0, false, "F"));
				} else {
					answers.add(0, new TestAnswer(0, 0, 0, false, "T"));
				}

				numAnswers = 2;
			}

			checkedAnswersAdapter = new CheckedAnswersArrayAdapter(this, R.layout.list_item_single_choice,
					answers, test.isEvaluated(), test.getFeedback(), answerType);

			testMakeList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			testMakeList.setAdapter(checkedAnswersAdapter);

			for(int i=0; i<numAnswers; i++) {
				a = answers.get(i);
				if(a.getAnswer().equals(answers.get(0).getUserAnswer())) {
					testMakeList.setItemChecked(i, true);
					break;
				}
			}

			testMakeList.setVisibility(View.VISIBLE);
		}

		if(test.isEvaluated() && (feedback.equals("eachResult") || feedback.equals("eachGoodBad"))) {
			textAnswer.setEnabled(false);
			textAnswer.setOnClickListener(null);

			if(feedback.equals("eachGoodBad")) {
				img.setImageResource(R.drawable.btn_check_buttonless_on);				
				if(!answerType.equals("TF") && !answerType.equals("multipleChoice")
						&& !answerType.equals("uniqueChoice")) {

					if(!answers.get(0).isCorrectAnswered()) {
						img.setImageResource(android.R.drawable.ic_delete);
					}

					img.setVisibility(View.VISIBLE);
				}
			}

			questionScore = test.getQuestionScore(pos);
			if(questionScore > 0) {
				score.setTextColor(getResources().getColor(R.color.green));
			} else if(questionScore < 0) {
				score.setTextColor(getResources().getColor(R.color.red));
			} else {
				score.setTextColor(Color.BLACK);
			}

			score.setText(df.format(questionScore));				
			score.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Reads the user answer of a question
	 * @param q Question to read the answer
	 */
	private void readUserAnswer(TestQuestion q) {
		ListView testMakeList = (ListView) findViewById(R.id.testMakeList);
		EditText textAnswer = (EditText) findViewById(R.id.testMakeEditText);
		List<TestAnswer> la = q.getAnswers();
		int checkedListCount, selectedPos;
		String answerType, userAnswer;
		SparseBooleanArray checkedItems;

		answerType = q.getAnswerType();
		if(answerType.equals("text")
				|| answerType.equals("int")
				|| answerType.equals("float")) {

			la.get(0).setUserAnswer(String.valueOf(textAnswer.getText()));
		} else if(answerType.equals("multipleChoice")) {
			checkedItems = testMakeList.getCheckedItemPositions();
			checkedListCount = checkedItems.size();
			for(int i=0; i<checkedListCount; i++) {
				la.get(i).setUserAnswer(Global.parseBoolString(checkedItems.get(i, false)));
			}
		} else {
			selectedPos = testMakeList.getCheckedItemPosition();
			if(selectedPos == -1) {
				userAnswer = "";
			} else {
				userAnswer = la.get(selectedPos).getAnswer();
			}

			la.get(0).setUserAnswer(userAnswer);
		}
	}

	/**
	 * Shows the test
	 */
	private void showTest() {
		final TextProgressBar bar;
		Button prev, next, eval;
		ImageView title_separator;
		final int size = test.getQuestions().size();

		setLayout(R.layout.tests_make_questions);
		prev = (Button) findViewById(R.id.testMakePrevButton);
		next = (Button) findViewById(R.id.testMakeNextButton);
		eval = (Button) findViewById(R.id.testEvaluateButton);
		title_separator = (ImageView) findViewById(R.id.title_sep_2);
		bar = (TextProgressBar) findViewById(R.id.test_questions_bar);

		bar.setMax(size);
		bar.setProgress(1);  
		bar.setText(1 + "/" + size);
		bar.setTextColor(Color.BLUE);
		bar.setTextSize(20);

		eval.setVisibility(View.VISIBLE);
		title_separator.setVisibility(View.VISIBLE);

		actualQuestion = 0;
		prev.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				TestQuestion question = test.getQuestionAndAnswers(actualQuestion);
				int pos;

				if(!test.isEvaluated()) {
					readUserAnswer(question);
				}

				actualQuestion--;				
				if(actualQuestion < 0) {
					actualQuestion = size-1;
				}

				pos = actualQuestion+1;

				showQuestion(actualQuestion);
				bar.setProgress(pos);  
				bar.setText(pos + "/" + size);
			}
		});

		next.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				TestQuestion question = test.getQuestionAndAnswers(actualQuestion);
				int pos;

				if(!test.isEvaluated()) {
					readUserAnswer(question);
				}

				actualQuestion++;
				actualQuestion %= size;
				pos = actualQuestion+1;

				showQuestion(actualQuestion);
				bar.setProgress(pos);  
				bar.setText(pos + "/" + size);
			}
		});

		showQuestion(0);
	}

	/**
	 * Generates the test 
	 */
	private void makeTest() {
		List<TestQuestion> questions;

		//Generates the test
		questions = dbHelper.getRandomCourseQuestionsByTagAndAnswerType(Global.getSelectedCourseCode(), tagsList, answerTypesList,
				numQuestions);
		if(!questions.isEmpty()) {			
			test.setQuestions(questions);

			//Shuffles related answers in a question if necessary
			for(TestQuestion q : questions) {
				if(q.getShuffle()) {
					q.shuffleAnswers();
				}
			}

			//Shows the test
			showTest();
		} else {
			Toast.makeText(this, R.string.testNoQuestionsMeetsSpecifiedCriteriaMsg, Toast.LENGTH_LONG);
			finish();
		}
	}

	/**
	 * Launches an action when evaluate button is pushed
	 * @param v Actual view
	 */
	public void onEvaluateClick(View v) {
		TextView textView;
		Button bt, evalBt;
		ImageView sep2;
		Float score, scoreDec;
		DecimalFormat df = new DecimalFormat("0.00");
		String feedback = test.getFeedback();

		readUserAnswer(test.getQuestionAndAnswers(actualQuestion));

		setLayout(R.layout.tests_make_results);
		if(!feedback.equals("nothing")) {
			if(!test.isEvaluated()) {
				test.evaluate();
				evalBt = (Button) findViewById(R.id.testEvaluateButton);
				sep2 = (ImageView) findViewById(R.id.title_sep_2);

				evalBt.setVisibility(View.GONE);
				sep2.setVisibility(View.GONE);
			}

			score = test.getTotalScore();
			scoreDec = (score/test.getQuestions().size())*10;

			textView = (TextView) findViewById(R.id.testResultsScore);
			textView.setText(df.format(score) + "/" + test.getQuestions().size() + "\n"
					+ df.format(scoreDec) + "/10");

			if(scoreDec < 5) {
				textView.setTextColor(getResources().getColor(R.color.red));
			}

			bt = (Button) findViewById(R.id.testResultsButton);
			if(feedback.equals("totalResult")) {
				bt.setEnabled(false);
				bt.setText(R.string.testNoDetailsMsg);
			}

			textView.setVisibility(View.VISIBLE);
			bt.setVisibility(View.VISIBLE);			
		} else {
			textView = (TextView) findViewById(R.id.testResultsText);
			textView.setText(R.string.testNoResultsMsg);
		}
	}

	/**
	 * Launches an action when show results details button is pushed
	 * @param v Actual view
	 */
	public void onShowResultsDetailsClick(View v) {
		Button evalBt, resBt;
		ImageView sep2, sep3;

		showTest();

		evalBt = (Button) findViewById(R.id.testEvaluateButton);
		sep2 = (ImageView) findViewById(R.id.title_sep_2);		
		resBt = (Button) findViewById(R.id.testShowResultsButton);
		sep3 = (ImageView) findViewById(R.id.title_sep_3);

		evalBt.setVisibility(View.GONE);
		sep2.setVisibility(View.GONE);
		resBt.setVisibility(View.VISIBLE);
		sep3.setVisibility(View.VISIBLE);
	}

	/* (non-Javadoc)
	 * @see es.ugr.swad.swadroid.modules.Module#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setLayout(R.layout.layout_with_action_bar);

		tagsAnswersTypeItemClickListener = new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {

				CheckedTextView chk = (CheckedTextView) v;
				ListView lv = (ListView) parent;
				int childCount = lv.getCount();
				SparseBooleanArray checkedItems = lv.getCheckedItemPositions();
				boolean allChecked = true;

				if(position == 0) {
					for(int i=1; i<childCount; i++) {
						lv.setItemChecked(i, !chk.isChecked());
					}
				} else {					
					for(int i=1; i<childCount; i++) {
						if(!checkedItems.get(i, false)) {
							allChecked = false;
						}
					}

					if (allChecked) {
						lv.setItemChecked(0, true);
					} else if(chk.isChecked()) {
						lv.setItemChecked(0, false);
					}
				}
			}			
		};

		tfAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		tfAdapter.add(getString(R.string.trueMsg));
		tfAdapter.add(getString(R.string.falseMsg));

		setResult(RESULT_OK);
	}

	/* (non-Javadoc)
	 * @see es.ugr.swad.swadroid.modules.Module#onStart()
	 */
	@Override
	protected void onStart() {
		Course c;
		int lastCourseSelected;

		super.onStart();
		prefs.getPreferences(getBaseContext());
		String selection ="id=" + Long.toString(Global.getSelectedCourseCode());
		Cursor dbCursor = dbHelper.getDb().getCursor(Global.DB_TABLE_TEST_CONFIG,selection,null);
		startManagingCursor(dbCursor);
		if(dbCursor.getCount() > 0) {			
			if(isDebuggable) {
				Log.d(TAG, "selectedCourseCode = " + Long.toString(Global.getSelectedCourseCode()));
			}

			test = (Test) dbHelper.getRow(Global.DB_TABLE_TEST_CONFIG, "id",
					Long.toString(Global.getSelectedCourseCode()));
		
			if(test != null) {
				setNumQuestions();
			} else {
				Toast.makeText(getBaseContext(), R.string.testNoQuestionsCourseMsg, Toast.LENGTH_LONG).show();
				finish();
			}
		} else {
			Toast.makeText(getBaseContext(), R.string.testNoQuestionsMsg, Toast.LENGTH_LONG).show();		
		}

	}

	/* (non-Javadoc)
	 * @see es.ugr.swad.swadroid.modules.Module#requestService()
	 */
	@Override
	protected void requestService() throws NoSuchAlgorithmException,
	IOException, XmlPullParserException, SoapFault,
	IllegalAccessException, InstantiationException {

	}

	/* (non-Javadoc)
	 * @see es.ugr.swad.swadroid.modules.Module#connect()
	 */
	@Override
	protected void connect() {

	}

	/* (non-Javadoc)
	 * @see es.ugr.swad.swadroid.modules.Module#postConnect()
	 */
	@Override
	protected void postConnect() {

	}

	/* (non-Javadoc)
	 * @see es.ugr.swad.swadroid.modules.Module#onError()
	 */
	@Override
	protected void onError() {

	}
}
