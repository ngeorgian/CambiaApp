package com.example.nick.cambiaapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import static com.example.nick.cambiaapp.R.id.cardFive;
import static com.example.nick.cambiaapp.R.id.cardFour;
import static com.example.nick.cambiaapp.R.id.cardOne;
import static com.example.nick.cambiaapp.R.id.cardSix;
import static com.example.nick.cambiaapp.R.id.cardThree;
import static com.example.nick.cambiaapp.R.id.cardTwo;
import static com.example.nick.cambiaapp.R.id.lastPlayedCard;

public class PlayScreen extends AppCompatActivity implements OnDragListener, OnTouchListener {


    //VARIABLE NAMES AND BASIC INITIALIZATION

    //For testing purposes
    private static final String TAG = "ngeorgian.cambia";
    //Numbers for adjusting player button widths
    final float twoPlayerWidth = 180, threePlayerWidth = 120, fourPlayerWidth = 90, fivePlayerWidth = 72;
    //The different names for the card ImageViews
    ImageView cardDrawn, lastPlayed, cardOneImageView, cardTwoImageView, cardThreeImageView, cardFourImageView, cardFiveImageView, cardSixImageView;
    String playerOneName = "";
    String playerTwoName = "";
    String playerThreeName = "";
    String playerFourName = "";
    String playerFiveName = "";
    String playerSixName = "";
    int playerCount = 0;
    //References to different player buttons that switch between hands
    Button playerOneButton;
    Button playerTwoButton;
    Button playerThreeButton;
    Button playerFourButton;
    Button playerFiveButton;
    Button playerSixButton;
    //Initialize readyButton and promptText (button that starts a player's turn)
    Button readyButton;
    TextView promptText;
    //Initialize In-Game Buttons
    Button drawCardButton;
    Button endTurnButton;
    Button fullSwapButton;
    //Initialize MessageBoard Button
    Button messageBoardButton;
    //Initialize the name and number for the player name displayed on top of screen (number used for referencing the player's hand)
    TextView currentPlayerName;
    int currentPlayerDisplay = 0;
    //Setup for creating the deck
    DeckUtilities du = new DeckUtilities();
    String[] strings = new String[56];
    Card[] deck = new Card[56];
    //Initializes withdraw pile and player hands variables as well as the deck status variable.
    Card[] withdrawPile = new Card[56];
    Card[][] playerHands = new Card[6][6];
    int deckStatus = 0;
    //Last played card && card drawn
    Card withdraw;
    Card next;
    //The player who's current turn it is
    int playerTurn = 1;
    //Booleans to help keep track of current game conditions:
    boolean cardDrew, discard, cardDrawnPlayed, playerReady,
            cardOneNull, cardTwoNull, cardThreeNull, cardFourNull, cardFiveNull, cardSixNull,
            lookSelf, lookOther, blindSwapSelf, blindSwapOther, fullSwapSelf, fullSwapOther, fullSwapSwitch = false;
    //Pointers for swap cards
    Card swapCard, swapCard2 = null;
    int swapPlayer, swapCardNumber, swap2Player, swapCard2Number = 0;     //Handler for two-second timer
    Handler tsTimer;
    //Strings for Message board
    String tempMessage, playerOneMessage, playerTwoMessage, playerThreeMessage, playerFourMessage, playerFiveMessage, playerSixMessage = "";
    //Queue that stores the messages for the message board
    int queueSize = 0;
    Queue<String> messageQueue = new LinkedList<>();

    //Method used to convert Dps to pxs. Obtained from:
    //http://stackoverflow.com/questions/4605527/converting-pixels-to-dp
    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }


    //GAME SETUP / INITIALIZATION

    //Overrides back button so players cannot exit the game via core phone functions (prevents accidental quitting)
    public void onBackPressed() {
    }


    //BELOW ARE BUTTON LISTENERS

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);

        //Hides the action bar
        getSupportActionBar().hide();

        //Obtains information from playerNameScreen
        Bundle playerNameData = getIntent().getExtras();
        playerCount = playerNameData.getInt("playerCount");
        playerOneName = playerNameData.getString("playerOneName");
        playerTwoName = playerNameData.getString("playerTwoName");

        //Initializes variables
        playerOneButton = (Button) findViewById(R.id.playerOneButton);
        playerTwoButton = (Button) findViewById(R.id.playerTwoButton);
        playerThreeButton = (Button) findViewById(R.id.playerThreeButton);
        playerFourButton = (Button) findViewById(R.id.playerFourButton);
        playerFiveButton = (Button) findViewById(R.id.playerFiveButton);
        playerSixButton = (Button) findViewById(R.id.playerSixButton);

        readyButton = (Button) findViewById(R.id.readyButton);
        promptText = (TextView) findViewById(R.id.readyText);

        drawCardButton = (Button) findViewById(R.id.drawCardButton);
        endTurnButton = (Button) findViewById(R.id.endTurnButton);
        fullSwapButton = (Button) findViewById(R.id.fullSwapButton);

        messageBoardButton = (Button) findViewById(R.id.messageBoardButton);

        cardDrawn = (ImageView) findViewById(R.id.cardDrawn);
        lastPlayed = (ImageView) findViewById(lastPlayedCard);
        cardOneImageView = (ImageView) findViewById(R.id.cardOne);
        cardTwoImageView = (ImageView) findViewById(R.id.cardTwo);
        cardThreeImageView = (ImageView) findViewById(R.id.cardThree);
        cardFourImageView = (ImageView) findViewById(R.id.cardFour);
        cardFiveImageView = (ImageView) findViewById(R.id.cardFive);
        cardSixImageView = (ImageView) findViewById(R.id.cardSix);

        currentPlayerName = (TextView) findViewById(R.id.currentPlayerName);
        currentPlayerName.setText(playerOneName);

        //Sets names and removes unused buttons
        playerOneButton.setText(playerOneName);
        playerTwoButton.setText(playerTwoName);

        if (playerCount >= 3) {
            playerThreeName = playerNameData.getString("playerThreeName");
            playerThreeButton.setText(playerThreeName);
        } else {
            playerThreeButton.setVisibility(View.GONE);
        }
        if (playerCount >= 4) {
            playerFourName = playerNameData.getString("playerFourName");
            playerFourButton.setText(playerFourName);
        } else {
            playerFourButton.setVisibility(View.GONE);
        }
        if (playerCount >= 5) {
            playerFiveName = playerNameData.getString("playerFiveName");
            playerFiveButton.setText(playerFiveName);
        } else {
            playerFiveButton.setVisibility(View.GONE);
        }
        if (playerCount == 6) {
            playerSixName = playerNameData.getString("playerSixName");
            playerSixButton.setText(playerSixName);
        } else {
            playerSixButton.setVisibility(View.GONE);
        }

        //Adjust button size
        if (playerCount == 2) {
            playerOneButton.getLayoutParams().width = (int) pxFromDp(this, twoPlayerWidth);
            playerTwoButton.getLayoutParams().width = (int) pxFromDp(this, twoPlayerWidth);
        }
        if (playerCount == 3) {
            playerOneButton.getLayoutParams().width = (int) pxFromDp(this, threePlayerWidth);
            playerTwoButton.getLayoutParams().width = (int) pxFromDp(this, threePlayerWidth);
            playerThreeButton.getLayoutParams().width = (int) pxFromDp(this, threePlayerWidth);
        }
        if (playerCount == 4) {
            playerOneButton.getLayoutParams().width = (int) pxFromDp(this, fourPlayerWidth);
            playerTwoButton.getLayoutParams().width = (int) pxFromDp(this, fourPlayerWidth);
            playerThreeButton.getLayoutParams().width = (int) pxFromDp(this, fourPlayerWidth);
            playerFourButton.getLayoutParams().width = (int) pxFromDp(this, fourPlayerWidth);
        }
        if (playerCount == 5) {
            playerOneButton.getLayoutParams().width = (int) pxFromDp(this, fivePlayerWidth);
            playerTwoButton.getLayoutParams().width = (int) pxFromDp(this, fivePlayerWidth);
            playerThreeButton.getLayoutParams().width = (int) pxFromDp(this, fivePlayerWidth);
            playerFourButton.getLayoutParams().width = (int) pxFromDp(this, fivePlayerWidth);
            playerFiveButton.getLayoutParams().width = (int) pxFromDp(this, fivePlayerWidth);
        }

        //Prepare deck strings to create the deck
        du.createStrings();
        strings = du.getStrings();

        //Assigns each card suit and number and passes along the card back image
        for (int counter = 0; counter < 54; counter++) {
            deck[counter] = new Card(strings[counter], ContextCompat.getDrawable(this, R.drawable.card_back));
        }

        //Assigns each card its corresponding image
        deck[0].setImage(ContextCompat.getDrawable(this, R.drawable.ace_of_clubs));
        deck[1].setImage(ContextCompat.getDrawable(this, R.drawable.ace_of_diamonds));
        deck[2].setImage(ContextCompat.getDrawable(this, R.drawable.ace_of_spades));
        deck[3].setImage(ContextCompat.getDrawable(this, R.drawable.ace_of_hearts));
        deck[4].setImage(ContextCompat.getDrawable(this, R.drawable.two_of_clubs));
        deck[5].setImage(ContextCompat.getDrawable(this, R.drawable.two_of_diamonds));
        deck[6].setImage(ContextCompat.getDrawable(this, R.drawable.two_of_spades));
        deck[7].setImage(ContextCompat.getDrawable(this, R.drawable.two_of_hearts));
        deck[8].setImage(ContextCompat.getDrawable(this, R.drawable.three_of_clubs));
        deck[9].setImage(ContextCompat.getDrawable(this, R.drawable.three_of_diamonds));
        deck[10].setImage(ContextCompat.getDrawable(this, R.drawable.three_of_spades));
        deck[11].setImage(ContextCompat.getDrawable(this, R.drawable.three_of_hearts));
        deck[12].setImage(ContextCompat.getDrawable(this, R.drawable.four_of_clubs));
        deck[13].setImage(ContextCompat.getDrawable(this, R.drawable.four_of_diamonds));
        deck[14].setImage(ContextCompat.getDrawable(this, R.drawable.four_of_spades));
        deck[15].setImage(ContextCompat.getDrawable(this, R.drawable.four_of_hearts));
        deck[16].setImage(ContextCompat.getDrawable(this, R.drawable.five_of_clubs));
        deck[17].setImage(ContextCompat.getDrawable(this, R.drawable.five_of_diamonds));
        deck[18].setImage(ContextCompat.getDrawable(this, R.drawable.five_of_spades));
        deck[19].setImage(ContextCompat.getDrawable(this, R.drawable.five_of_hearts));
        deck[20].setImage(ContextCompat.getDrawable(this, R.drawable.six_of_clubs));
        deck[21].setImage(ContextCompat.getDrawable(this, R.drawable.six_of_diamonds));
        deck[22].setImage(ContextCompat.getDrawable(this, R.drawable.six_of_spades));
        deck[23].setImage(ContextCompat.getDrawable(this, R.drawable.six_of_hearts));
        deck[24].setImage(ContextCompat.getDrawable(this, R.drawable.seven_of_clubs));
        deck[25].setImage(ContextCompat.getDrawable(this, R.drawable.seven_of_diamonds));
        deck[26].setImage(ContextCompat.getDrawable(this, R.drawable.seven_of_spades));
        deck[27].setImage(ContextCompat.getDrawable(this, R.drawable.seven_of_hearts));
        deck[28].setImage(ContextCompat.getDrawable(this, R.drawable.eight_of_clubs));
        deck[29].setImage(ContextCompat.getDrawable(this, R.drawable.eight_of_diamonds));
        deck[30].setImage(ContextCompat.getDrawable(this, R.drawable.eight_of_spades));
        deck[31].setImage(ContextCompat.getDrawable(this, R.drawable.eight_of_hearts));
        deck[32].setImage(ContextCompat.getDrawable(this, R.drawable.nine_of_clubs));
        deck[33].setImage(ContextCompat.getDrawable(this, R.drawable.nine_of_diamonds));
        deck[34].setImage(ContextCompat.getDrawable(this, R.drawable.nine_of_spades));
        deck[35].setImage(ContextCompat.getDrawable(this, R.drawable.nine_of_hearts));
        deck[36].setImage(ContextCompat.getDrawable(this, R.drawable.ten_of_clubs));
        deck[37].setImage(ContextCompat.getDrawable(this, R.drawable.ten_of_diamonds));
        deck[38].setImage(ContextCompat.getDrawable(this, R.drawable.ten_of_spades));
        deck[39].setImage(ContextCompat.getDrawable(this, R.drawable.ten_of_hearts));
        deck[40].setImage(ContextCompat.getDrawable(this, R.drawable.jack_of_clubs));
        deck[41].setImage(ContextCompat.getDrawable(this, R.drawable.jack_of_diamonds));
        deck[42].setImage(ContextCompat.getDrawable(this, R.drawable.jack_of_spades));
        deck[43].setImage(ContextCompat.getDrawable(this, R.drawable.jack_of_hearts));
        deck[44].setImage(ContextCompat.getDrawable(this, R.drawable.queen_of_clubs));
        deck[45].setImage(ContextCompat.getDrawable(this, R.drawable.queen_of_diamonds));
        deck[46].setImage(ContextCompat.getDrawable(this, R.drawable.queen_of_spades));
        deck[47].setImage(ContextCompat.getDrawable(this, R.drawable.queen_of_hearts));
        deck[48].setImage(ContextCompat.getDrawable(this, R.drawable.king_of_clubs));
        deck[49].setImage(ContextCompat.getDrawable(this, R.drawable.king_of_diamonds));
        deck[50].setImage(ContextCompat.getDrawable(this, R.drawable.king_of_spades));
        deck[51].setImage(ContextCompat.getDrawable(this, R.drawable.king_of_hearts));
        deck[52].setImage(ContextCompat.getDrawable(this, R.drawable.joker));
        deck[53].setImage(ContextCompat.getDrawable(this, R.drawable.joker));

        //Shuffles the deck
        deck = du.shuffleDeck(deck);

        //Distribute cards to each player
        for (int player = 0; player < playerCount; player++) {
            for (int card = 0; card < 4; card++) {
                playerHands[player][card] = deck[deckStatus];
                deckStatus++;
            }
        }

        //Assigns lastPlayed card to the next card in the deck
        withdraw = deck[deckStatus];
        deckStatus++;
        withdraw.setRevealed(true);
        lastPlayed.setImageDrawable(withdraw.getImage());

        promptText.setText(playerOneName + "s turn");

        //Sets player cards and deck cards to be moved
        findViewById(R.id.cardOne).setOnTouchListener(this);
        findViewById(R.id.cardTwo).setOnTouchListener(this);
        findViewById(R.id.cardThree).setOnTouchListener(this);
        findViewById(R.id.cardFour).setOnTouchListener(this);
        findViewById(R.id.cardFive).setOnTouchListener(this);
        findViewById(R.id.cardSix).setOnTouchListener(this);
        findViewById(R.id.cardDrawn).setOnTouchListener(this);
        findViewById(R.id.lastPlayedCard).setOnTouchListener(this);

        //Sets player cards and lastPlayedCard to be replaced
        findViewById(R.id.cardOne).setOnDragListener(this);
        findViewById(R.id.cardTwo).setOnDragListener(this);
        findViewById(R.id.cardThree).setOnDragListener(this);
        findViewById(R.id.cardFour).setOnDragListener(this);
        findViewById(R.id.cardFive).setOnDragListener(this);
        findViewById(R.id.cardSix).setOnDragListener(this);
        findViewById(lastPlayedCard).setOnDragListener(this);

        //Timer for incorrect card touches
        tsTimer = new Handler();

        //Removes penalty card image views
        playerButtonClick(1);
        playerButtonClick(0);
    }

    //Begins a player's turn by removing unnecessary buttons / text and displaying draw card button
    public void onReadyButtonClick(View view) {
        playerReady = true;
        readyButton.setVisibility(View.INVISIBLE);
        promptText.setVisibility(View.INVISIBLE);
        //TESTING
        /*
        String boldText = "id";
        String normalText = "name";
        SpannableString str = new SpannableString(boldText + normalText);
        str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        promptText.setText(str);
        promptText.setVisibility(view.VISIBLE); */

        drawCardButton.setVisibility(View.VISIBLE);
        updateImageViews();
    }

    //If a player decides to draw a card
    public void onDrawCardClick(View view) {
        cardDrew = true;
        drawCardButton.setVisibility(View.INVISIBLE);
        next = deck[deckStatus];
        next.setRevealed(true);
        cardDrawn.setImageDrawable(next.getImage());
        deckStatus++;
    }

    //If player decides to end turn (resets all game conditional variables)
    public void onEndTurnButtonClick(View view) {
        playerReady = false;
        cardDrew = false;
        cardDrawnPlayed = false;
        discard = false;
        blindSwapOther = false;
        blindSwapSelf = false;
        lookOther = false;
        lookSelf = false;
        fullSwapSelf = false;
        fullSwapOther = false;
        fullSwapSwitch = false;
        endTurnButton.setVisibility(View.INVISIBLE);
        fullSwapButton.setVisibility(View.INVISIBLE);

        //Reset all views
        cardDrawn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.card_back));

        //Reset all player cards to not revealed
        for (int player = 0; player < playerCount; player++) {
            for (int card = 0; card < 6; card++) {
                if (playerHands[player][card] != null) playerHands[player][card].setRevealed(false);
            }
        }

        //Updates the queue for the message board
        if (messageQueue.size() == playerCount - 1) {
            messageQueue.poll();
            queueSize--;
        }

        addMessage(tempMessage);
        updateMessages(0);
        tempMessage = "";

        updateImageViews();
        readyButton.setVisibility(View.VISIBLE);
        setReadyText();

        //Changes screen to the next player's deck (for convenience)
        if (playerTurn == 1) onPlayerOneButtonClick(view);
        if (playerTurn == 2) onPlayerTwoButtonClick(view);
        if (playerTurn == 3) onPlayerThreeButtonClick(view);
        if (playerTurn == 4) onPlayerFourButtonClick(view);
        if (playerTurn == 5) onPlayerFiveButtonClick(view);
        if (playerTurn == 6) onPlayerSixButtonClick(view);
        promptText.setVisibility(View.VISIBLE);

    }

    //If player plays a King and decides to swap the cards
    public void onFullSwapButtonClick(View view) {
        blindSwap(0, 0); //The input numbers don't matter since if-condition in method handles the event
        fullSwapButton.setVisibility(View.INVISIBLE);
    }

    //If the player wants to view the message board
    public void onMessageBoardButtonClick(View view) {

        Intent messageBoardIntent = new Intent(this, MessageBoard.class);

        Queue<SpannableString> tempQueue = deepCopyMessageQueue();
        String messageOne = "", messageTwo = "", messageThree = "", messageFour = "", messageFive = "";

        //Takes the copy of the queue and places each dequeued String into its proper message
        while (!tempQueue.isEmpty()) {
            if (messageOne.equals("")) messageOne = tempQueue.poll().toString();
            else if (messageTwo.equals("")) messageTwo = tempQueue.poll().toString();
            else if (messageThree.equals("")) messageThree = tempQueue.poll().toString();
            else if (messageFour.equals("")) messageFour = tempQueue.poll().toString();
            else if (messageFive.equals("")) messageFive = tempQueue.poll().toString();
        }

        messageBoardIntent.putExtra("messageOne", messageOne);
        messageBoardIntent.putExtra("messageTwo", messageTwo);
        messageBoardIntent.putExtra("messageThree", messageThree);
        messageBoardIntent.putExtra("messageFour", messageFour);
        messageBoardIntent.putExtra("messageFive", messageFive);

        startActivityForResult(messageBoardIntent, 1);
    }

    //Player Button Listeners
    public void onPlayerOneButtonClick(View view) {
        if (!playerOneName.equals(currentPlayerName.toString()))
            currentPlayerName.setText(playerOneName);
        playerButtonClick(0);
    }

    public void onPlayerTwoButtonClick(View view) {
        if (!playerTwoName.equals(currentPlayerName.toString()))
            currentPlayerName.setText(playerTwoName);
        playerButtonClick(1);
    }

    public void onPlayerThreeButtonClick(View view) {
        if (!playerThreeName.equals(currentPlayerName.toString()))
            currentPlayerName.setText(playerThreeName);
        playerButtonClick(2);
    }

    public void onPlayerFourButtonClick(View view) {
        if (!playerFourName.equals(currentPlayerName.toString()))
            currentPlayerName.setText(playerFourName);
        playerButtonClick(3);
    }

    public void onPlayerFiveButtonClick(View view) {
        if (!playerFiveName.equals(currentPlayerName.toString()))
            currentPlayerName.setText(playerFiveName);
        playerButtonClick(4);
    }

    public void onPlayerSixButtonClick(View view) {
        if (!playerSixName.equals(currentPlayerName.toString()))
            currentPlayerName.setText(playerSixName);
        playerButtonClick(5);
    }

    //Allows for actions to be taken when a card is dropped on a certain view
    public boolean onDrag(View v, DragEvent event) {

        if (playerReady == true) {

            if (event.getAction() == DragEvent.ACTION_DROP) {
                View view = (View) event.getLocalState();

                if (view == null) return false;

                //If the player decides to take the last played card instead of drawing a card
                if (cardDrew == false) {
                    if (view == lastPlayed) {
                        if (v.getId() == cardOne) takeLastPlayed(0);
                        if (v.getId() == cardTwo) takeLastPlayed(1);
                        if (v.getId() == cardThree) takeLastPlayed(2);
                        if (v.getId() == cardFour) takeLastPlayed(3);
                        if (v.getId() == cardFive) takeLastPlayed(4);
                        if (v.getId() == cardSix) takeLastPlayed(5);

                        drawCardButton.setVisibility(View.INVISIBLE);
                        endTurnButton.setVisibility(View.VISIBLE);
                    }
                }
                //If player decides to replace drawn card with one of their own
                if (view == cardDrawn) {
                    if (v.getId() == cardOne && playerHands[currentPlayerDisplay][0] != null) {
                        replaceWithNext(0);
                        endTurnButton.setVisibility(View.VISIBLE);
                    }
                    if (v.getId() == cardTwo && playerHands[currentPlayerDisplay][1] != null) {
                        replaceWithNext(1);
                        endTurnButton.setVisibility(View.VISIBLE);
                    }
                    if (v.getId() == cardThree && playerHands[currentPlayerDisplay][2] != null) {
                        replaceWithNext(2);
                        endTurnButton.setVisibility(View.VISIBLE);
                    }
                    if (v.getId() == cardFour && playerHands[currentPlayerDisplay][3] != null) {
                        replaceWithNext(3);
                        endTurnButton.setVisibility(View.VISIBLE);
                    }
                    if (v.getId() == cardFive && playerHands[currentPlayerDisplay][4] != null) {
                        replaceWithNext(4);
                        endTurnButton.setVisibility(View.VISIBLE);
                    }
                    if (v.getId() == cardSix && playerHands[currentPlayerDisplay][5] != null) {
                        replaceWithNext(5);
                        endTurnButton.setVisibility(View.VISIBLE);
                    }
                }

                if (v.getId() == lastPlayedCard) {

                    //If player plays card from drawn card
                    if (view == cardDrawn) {
                        //Seven or eight is player looks at one of his/her own card
                        if (next.getString().contains("7") || next.getString().contains("8")) {
                            promptText.setText("Look at one of your own cards");
                            promptText.setVisibility(View.VISIBLE);
                            lookSelf = true;
                            updateMessages(3, next.getString());
                        }

                        //Nine or ten is player looks at someone else's card
                        if (next.getString().contains("9") || next.getString().contains("10")) {
                            promptText.setText("Look at someone else's card");
                            promptText.setVisibility(View.VISIBLE);
                            lookOther = true;
                        }

                        //Jack or queen is blind swap
                        if (next.getString().contains("Jack") || next.getString().contains("Queen")) {
                            promptText.setText("Select one of your cards to be swapped");
                            promptText.setVisibility(View.VISIBLE);
                            blindSwapSelf = true;
                        }

                        //King is visible swap
                        if (next.getString().contains("King")) {
                            promptText.setText("Look at one of your own cards");
                            promptText.setVisibility(View.VISIBLE);
                            fullSwapSelf = true;
                        }

                        withdraw = next;
                        withdraw.setRevealed(true);
                        lastPlayed.setImageDrawable(withdraw.getImage());
                        cardDrawn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.card_back));
                        endTurnButton.setVisibility(View.VISIBLE);
                        cardDrawnPlayed = true;
                    }

                    //If player tries to discard
                    if (view == cardOneImageView && discard == false) {
                        if (compareCards(playerHands[currentPlayerDisplay][0].getString(), withdraw.getString()) == 0) {
                            discard(0);
                        }
                    }
                    if (view == cardTwoImageView && discard == false) {
                        if (compareCards(playerHands[currentPlayerDisplay][1].getString(), withdraw.getString()) == 0) {
                            discard(1);
                        }
                    }
                    if (view == cardThreeImageView && discard == false) {
                        if (compareCards(playerHands[currentPlayerDisplay][2].getString(), withdraw.getString()) == 0) {
                            discard(2);
                        }
                    }
                    if (view == cardFourImageView && discard == false) {
                        if (compareCards(playerHands[currentPlayerDisplay][3].getString(), withdraw.getString()) == 0) {
                            discard(3);
                        }
                    }
                    if (view == cardFiveImageView && discard == false) {
                        if (compareCards(playerHands[currentPlayerDisplay][4].getString(), withdraw.getString()) == 0) {
                            discard(4);
                        }
                    }
                    if (view == cardSixImageView && discard == false) {
                        if (compareCards(playerHands[currentPlayerDisplay][5].getString(), withdraw.getString()) == 0) {
                            discard(5);
                        }
                    }
                }
                //make view visible as we set visibility to invisible while starting drag
                view.setVisibility(View.VISIBLE);
            }
            return true;

        }
        return false;
    }


    // BELOW ARE HELPER METHODS

    //Allows cards to be movable and makes shadow
    public boolean onTouch(View view, MotionEvent event) {

        //Prevents players from playing null cards
        if (view == cardOneImageView && cardOneNull == true) return false;
        if (view == cardTwoImageView && cardTwoNull == true) return false;
        if (view == cardThreeImageView && cardThreeNull == true) return false;
        if (view == cardFourImageView && cardFourNull == true) return false;
        if (view == cardFiveImageView && cardFiveNull == true) return false;
        if (view == cardSixImageView && cardSixNull == true) return false;
        if (view == lastPlayed && (playerReady == false || cardDrew == true)) return false;
        if (view == cardDrawn && cardDrawn.getDrawable() == null) return false;

        //If a player is using a self-look power (played a seven or eight)
        if (lookSelf == true || fullSwapSelf == true) {
            //Card One
            if (view == cardOneImageView && cardOneImageView.getDrawable() == playerHands[playerTurn - 1][0].getImage()) {
                revealCard(playerTurn - 1, 0);
                return true;
            } else if (view == cardOneImageView && cardOneImageView.getDrawable() != playerHands[playerTurn - 1][0].getImage())
                faultText(0);

            //Card Two
            if (view == cardTwoImageView && cardTwoImageView.getDrawable() == playerHands[playerTurn - 1][1].getImage()) {
                revealCard(playerTurn - 1, 1);
                return true;
            } else if (view == cardTwoImageView && cardTwoImageView.getDrawable() != playerHands[playerTurn - 1][1].getImage())
                faultText(0);

            //Card Three
            if (view == cardThreeImageView && cardThreeImageView.getDrawable() == playerHands[playerTurn - 1][2].getImage()) {
                revealCard(playerTurn - 1, 2);
                return true;
            } else if (view == cardThreeImageView && cardThreeImageView.getDrawable() != playerHands[playerTurn - 1][2].getImage())
                faultText(0);

            //Card Four
            if (view == cardFourImageView && cardFourImageView.getDrawable() == playerHands[playerTurn - 1][3].getImage()) {
                revealCard(playerTurn - 1, 3);
                return true;
            } else if (view == cardFourImageView && cardFourImageView.getDrawable() != playerHands[playerTurn - 1][3].getImage())
                faultText(0);

            //Card Five
            if (view == cardFiveImageView && cardFiveImageView.getDrawable() == playerHands[playerTurn - 1][4].getImage()) {
                revealCard(playerTurn - 1, 4);
                return true;
            } else if (view == cardFiveImageView && cardFiveImageView.getDrawable() != playerHands[playerTurn - 1][4].getImage())
                faultText(0);

            //Card Six
            if (view == cardSixImageView && cardSixImageView.getDrawable() == playerHands[playerTurn - 1][5].getImage()) {
                revealCard(playerTurn - 1, 5);
                return true;
            } else if (view == cardSixImageView && cardSixImageView.getDrawable() != playerHands[playerTurn - 1][5].getImage())
                faultText(0);
        }

        //If a player is using an other-look power (played a nine or ten)
        if (lookOther == true || fullSwapOther == true) {
            //Card One
            if (view == cardOneImageView && cardOneImageView.getDrawable() != playerHands[playerTurn - 1][0].getImage()) {
                revealCard(currentPlayerDisplay, 0);
                return true;
            } else if (view == cardOneImageView && cardOneImageView.getDrawable() == playerHands[playerTurn - 1][0].getImage())
                faultText(1);

            //Card Two
            if (view == cardTwoImageView && cardTwoImageView.getDrawable() != playerHands[playerTurn - 1][1].getImage()) {
                revealCard(currentPlayerDisplay, 1);
                return true;
            } else if (view == cardTwoImageView && cardTwoImageView.getDrawable() == playerHands[playerTurn - 1][1].getImage())
                faultText(1);

            //Card Three
            if (view == cardThreeImageView && cardThreeImageView.getDrawable() != playerHands[playerTurn - 1][2].getImage()) {
                revealCard(currentPlayerDisplay, 2);
                return true;
            } else if (view == cardThreeImageView && cardThreeImageView.getDrawable() == playerHands[playerTurn - 1][2].getImage())
                faultText(1);

            //Card Four
            if (view == cardFourImageView && cardFourImageView.getDrawable() != playerHands[playerTurn - 1][3].getImage()) {
                revealCard(currentPlayerDisplay, 3);
                return true;
            } else if (view == cardFourImageView && cardFourImageView.getDrawable() == playerHands[playerTurn - 1][3].getImage())
                faultText(1);

            //Card Five
            if (view == cardFiveImageView && cardFiveImageView.getDrawable() != playerHands[playerTurn - 1][4].getImage()) {
                revealCard(currentPlayerDisplay, 4);
                return true;
            } else if (view == cardFiveImageView && cardFiveImageView.getDrawable() == playerHands[playerTurn - 1][4].getImage())
                faultText(1);

            //Card Six
            if (view == cardSixImageView && cardSixImageView.getDrawable() != playerHands[playerTurn - 1][5].getImage()) {
                revealCard(currentPlayerDisplay, 5);
                return true;
            } else if (view == cardSixImageView && cardSixImageView.getDrawable() == playerHands[playerTurn - 1][5].getImage())
                faultText(1);
        }

        //First part of blindSwap
        if (blindSwapSelf == true) {
            //Card One
            if (view == cardOneImageView && cardOneImageView.getDrawable() == playerHands[playerTurn - 1][0].getImage()) {
                blindSwap(playerTurn - 1, 0);
                promptText.setText("Select one of your opponents cards to be swapped");
                return true;
            } else if (view == cardOneImageView && cardOneImageView.getDrawable() != playerHands[playerTurn - 1][0].getImage())
                faultText(0);

            //Card Two
            if (view == cardTwoImageView && cardTwoImageView.getDrawable() == playerHands[playerTurn - 1][1].getImage()) {
                blindSwap(playerTurn - 1, 1);
                promptText.setText("Select one of your opponents cards to be swapped");
                return true;
            } else if (view == cardTwoImageView && cardTwoImageView.getDrawable() != playerHands[playerTurn - 1][1].getImage())
                faultText(0);

            //Card Three
            if (view == cardThreeImageView && cardThreeImageView.getDrawable() == playerHands[playerTurn - 1][2].getImage()) {
                blindSwap(playerTurn - 1, 2);
                promptText.setText("Select one of your opponents cards to be swapped");
                return true;
            } else if (view == cardThreeImageView && cardThreeImageView.getDrawable() != playerHands[playerTurn - 1][2].getImage())
                faultText(0);

            //Card Four
            if (view == cardFourImageView && cardFourImageView.getDrawable() == playerHands[playerTurn - 1][3].getImage()) {
                blindSwap(playerTurn - 1, 3);
                promptText.setText("Select one of your opponents cards to be swapped");
                return true;
            } else if (view == cardFourImageView && cardFourImageView.getDrawable() != playerHands[playerTurn - 1][3].getImage())
                faultText(0);

            //Card Five
            if (view == cardFiveImageView && cardFiveImageView.getDrawable() == playerHands[playerTurn - 1][4].getImage()) {
                blindSwap(playerTurn - 1, 4);
                promptText.setText("Select one of your opponents cards to be swapped");
                return true;
            } else if (view == cardFiveImageView && cardFiveImageView.getDrawable() != playerHands[playerTurn - 1][4].getImage())
                faultText(0);

            //Card Six
            if (view == cardSixImageView && cardSixImageView.getDrawable() == playerHands[playerTurn - 1][5].getImage()) {
                blindSwap(playerTurn - 1, 5);
                promptText.setText("Select one of your opponents cards to be swapped");
                return true;
            } else if (view == cardSixImageView && cardSixImageView.getDrawable() != playerHands[playerTurn - 1][5].getImage())
                faultText(0);
        }

        //Second part of blindSwap
        if (blindSwapOther == true) {
            //Card One
            if (view == cardOneImageView && cardOneImageView.getDrawable() != playerHands[playerTurn - 1][0].getImage()) {
                blindSwap(currentPlayerDisplay, 0);
                return true;
            } else if (view == cardOneImageView && cardOneImageView.getDrawable() == playerHands[playerTurn - 1][0].getImage() && promptText.getText().toString().compareTo("Cards have been swapped") != 0)
                faultText(1);

            //Card Two
            if (view == cardTwoImageView && cardTwoImageView.getDrawable() != playerHands[playerTurn - 1][1].getImage()) {
                blindSwap(currentPlayerDisplay, 1);
                return true;
            } else if (view == cardTwoImageView && cardTwoImageView.getDrawable() == playerHands[playerTurn - 1][1].getImage() && promptText.getText().toString().compareTo("Cards have been swapped") != 0)
                faultText(1);

            //Card Three
            if (view == cardThreeImageView && cardThreeImageView.getDrawable() != playerHands[playerTurn - 1][2].getImage()) {
                blindSwap(currentPlayerDisplay, 2);
                return true;
            } else if (view == cardThreeImageView && cardThreeImageView.getDrawable() == playerHands[playerTurn - 1][2].getImage() && promptText.getText().toString().compareTo("Cards have been swapped") != 0)
                faultText(1);

            //Card Four
            if (view == cardFourImageView && cardFourImageView.getDrawable() != playerHands[playerTurn - 1][3].getImage()) {
                blindSwap(currentPlayerDisplay, 3);
                return true;
            } else if (view == cardFourImageView && cardFourImageView.getDrawable() == playerHands[playerTurn - 1][3].getImage() && promptText.getText().toString().compareTo("Cards have been swapped") != 0)
                faultText(1);

            //Card Five
            if (view == cardFiveImageView && cardFiveImageView.getDrawable() != playerHands[playerTurn - 1][4].getImage()) {
                blindSwap(currentPlayerDisplay, 4);
                return true;
            } else if (view == cardFiveImageView && cardFiveImageView.getDrawable() == playerHands[playerTurn - 1][4].getImage() && promptText.getText().toString().compareTo("Cards have been swapped") != 0)
                faultText(1);

            //Card Six
            if (view == cardSixImageView && cardSixImageView.getDrawable() != playerHands[playerTurn - 1][5].getImage()) {
                blindSwap(currentPlayerDisplay, 5);
                return true;
            } else if (view == cardSixImageView && cardSixImageView.getDrawable() == playerHands[playerTurn - 1][5].getImage() && promptText.getText().toString().compareTo("Cards have been swapped") != 0)
                faultText(1);
        }


        //IF a card is dragged, then it creates a shadow
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(null, shadowBuilder, view, 0);
            view.setVisibility(View.INVISIBLE);
            return true;
        }
        view.setVisibility(View.VISIBLE);
        return false;
    }

    //Helper Method that sets readyText to next player name
    private void setReadyText() {
        if (playerTurn == 1) {
            promptText.setText(playerTwoName + "s turn");
            playerTurn++;
        } else if (playerTurn == 2 && playerCount > 2) {
            promptText.setText(playerThreeName + "s turn");
            playerTurn++;
        } else if (playerTurn == 3 && playerCount > 3) {
            promptText.setText(playerFourName + "s turn");
            playerTurn++;
        } else if (playerTurn == 4 && playerCount > 4) {
            promptText.setText(playerFiveName + "s turn");
            playerTurn++;
        } else if (playerTurn == 5 && playerCount > 5) {
            promptText.setText(playerSixName + "s turn");
            playerTurn++;
        } else {
            promptText.setText(playerOneName + "s turn");
            playerTurn = 1;
        }
    }

    //Helper method that compares 2 cards' values
    private int compareCards(String card1, String card2) {
        String temp1 = "", temp2 = "";
        int counter = 0;
        char tempChar = card1.charAt(counter);
        while (tempChar != ' ') {
            temp1 += tempChar;
            tempChar = card1.charAt(counter + 1);
            counter++;
        }
        counter = 0;
        tempChar = card2.charAt(counter);
        while (tempChar != ' ') {
            temp2 += tempChar;
            tempChar = card2.charAt(counter + 1);
            counter++;
        }
        return temp1.compareTo(temp2);
    }

    //Helper method that reveals a card until the end of the player's turn
    private void revealCard(int player, int card) {
        updateMessages(4, card);
        playerHands[player][card].setRevealed(true);
        if (card == 0) cardOneImageView.setImageDrawable(playerHands[player][card].getImage());
        if (card == 1) cardTwoImageView.setImageDrawable(playerHands[player][card].getImage());
        if (card == 2) cardThreeImageView.setImageDrawable(playerHands[player][card].getImage());
        if (card == 3) cardFourImageView.setImageDrawable(playerHands[player][card].getImage());
        if (card == 4) cardFiveImageView.setImageDrawable(playerHands[player][card].getImage());
        if (card == 5) cardSixImageView.setImageDrawable(playerHands[player][card].getImage());

        lookSelf = false;
        lookOther = false;

        //Actions taken depending on point in the FullSwap process. NOTE: timers are to prevent inappropriate fault texts from appearing
        if (fullSwapSelf == true) {
            fullSwapSelf = false;
            tsTimer.postDelayed(new Runnable() {
                public void run() {
                    fullSwapOther = true;
                }
            }, 100);
            promptText.setText("Look at someone else's card");
            swapCard = playerHands[player][card];
            swapCardNumber = card;
            swapPlayer = player;
        } else if (fullSwapOther == true) {
            fullSwapOther = false;
            tsTimer.postDelayed(new Runnable() {
                public void run() {
                    fullSwapSwitch = true;
                }
            }, 100);
            promptText.setText("Click swap to exhange cards. Otherwise, click end turn");
            fullSwapButton.setVisibility(View.VISIBLE);
            swapCard2 = playerHands[player][card];
            swapCard2Number = card;
            swap2Player = player;
        } else {
            promptText.setVisibility(View.INVISIBLE);
            endTurnButton.setVisibility(View.VISIBLE);
        }
    }

    //Helper method that switches cards once 2 have been selected
    private void blindSwap(int player, int card) {
        // BlindSwap: If selected card is the first card
        if (blindSwapSelf == true) {
            swapCard = playerHands[player][card];
            swapPlayer = player;
            swapCardNumber = card;
            blindSwapSelf = false;
            tsTimer.postDelayed(new Runnable() {
                public void run() {
                    blindSwapOther = true;
                }
            }, 100);
        }//Prevents faultText from appearing inappropriately

        //BlindSwap: If the selected card is the second card ; FullSwap: Switches cards
        else {
            if (blindSwapOther == true) {
                blindSwapOther = false;
                Card tempCard = playerHands[player][card];

                playerHands[player][card] = swapCard;
                playerHands[swapPlayer][swapCardNumber] = tempCard;
            } else if (fullSwapSwitch == true) {
                fullSwapSwitch = false;

                playerHands[swap2Player][swapCard2Number] = swapCard;
                playerHands[swapPlayer][swapCardNumber] = swapCard2;

                //Resets the view to the proper cards
                if (currentPlayerDisplay == 0) {
                    playerButtonClick(1);
                    playerButtonClick(0);
                } else {
                    int tempInt = currentPlayerDisplay;
                    playerButtonClick(0);
                    playerButtonClick(tempInt);
                }
            }

            if (card == 0) cardOneImageView.setImageDrawable(playerHands[player][card].getImage());
            if (card == 1) cardTwoImageView.setImageDrawable(playerHands[player][card].getImage());
            if (card == 2)
                cardThreeImageView.setImageDrawable(playerHands[player][card].getImage());
            if (card == 3) cardFourImageView.setImageDrawable(playerHands[player][card].getImage());
            if (card == 4) cardFiveImageView.setImageDrawable(playerHands[player][card].getImage());
            if (card == 5) cardSixImageView.setImageDrawable(playerHands[player][card].getImage());

            promptText.setText("Cards have been swapped");
            endTurnButton.setVisibility(View.VISIBLE);
        }
    }

    //Changes promptText depending on what the player did wrong
    private void faultText(int key) {
        final String temp = promptText.getText().toString();
        if (key == 0) {
            promptText.setText("Must be a card from your hand");
            promptText.setTextColor(Color.RED);

            //Waits two seconds, then changes the text back if the player has not yet ended the turn
            tsTimer.postDelayed(new Runnable() {
                public void run() {
                    if (promptText.getText().toString().compareTo("Must be a card from your hand") == 0)
                        promptText.setText(temp);
                    promptText.setTextColor(Color.BLACK);
                }
            }, 2000);
        }
        if (key == 1) {
            promptText.setText("Must be a card from someone else's hand");
            promptText.setTextColor(Color.RED);

            tsTimer.postDelayed(new Runnable() {
                public void run() {
                    if (promptText.getText().toString().compareTo("Must be a card from someone else's hand") == 0)
                        promptText.setText(temp);
                    promptText.setTextColor(Color.BLACK);
                }
            }, 2000);
        }
    }

    //Helper class for playerButtons
    private void playerButtonClick(int player) {
        //For every card, if it's null (a.k.a. the card has been played), the conditions are set so the display is null and the listener is silenced
        if (playerHands[player][0] == null) {
            cardOneNull = true;
            cardOneImageView.setImageDrawable(null);
        } else {
            cardOneNull = false;
            cardOneImageView.setImageDrawable(playerHands[player][0].getImage());
        }

        if (playerHands[player][1] == null) {
            cardTwoNull = true;
            cardTwoImageView.setImageDrawable(null);
        } else {
            cardTwoNull = false;
            cardTwoImageView.setImageDrawable(playerHands[player][1].getImage());
        }

        if (playerHands[player][2] == null) {
            cardThreeNull = true;
            cardThreeImageView.setImageDrawable(null);
        } else {
            cardThreeNull = false;
            cardThreeImageView.setImageDrawable(playerHands[player][2].getImage());
        }

        if (playerHands[player][3] == null) {
            cardFourNull = true;
            cardFourImageView.setImageDrawable(null);
        } else {
            cardFourNull = false;
            cardFourImageView.setImageDrawable(playerHands[player][3].getImage());
        }

        if (playerHands[player][4] == null) {
            cardFiveNull = true;
            cardFiveImageView.setImageDrawable(null);
        } else {
            cardFiveNull = false;
            cardFiveImageView.setImageDrawable(playerHands[player][4].getImage());
        }

        if (playerHands[player][5] == null) {
            cardSixNull = true;
            cardSixImageView.setImageDrawable(null);
        } else {
            cardSixNull = false;
            cardSixImageView.setImageDrawable(playerHands[player][5].getImage());
        }

        currentPlayerDisplay = player;
    }

    //Helper method for discarding cards
    private void discard(int card) {
        playerHands[currentPlayerDisplay][card].setRevealed(true);
        withdraw = playerHands[currentPlayerDisplay][card];
        lastPlayed.setImageDrawable(playerHands[currentPlayerDisplay][card].getImage());

        if (card == 0) cardOneImageView.setImageDrawable(null);
        if (card == 1) cardTwoImageView.setImageDrawable(null);
        if (card == 2) cardThreeImageView.setImageDrawable(null);
        if (card == 3) cardFourImageView.setImageDrawable(null);
        if (card == 4) cardFiveImageView.setImageDrawable(null);
        if (card == 5) cardSixImageView.setImageDrawable(null);

        playerHands[currentPlayerDisplay][card] = null;
        if (cardDrawnPlayed == true) discard = true;
    }

    //Helper method that handles if a player decides to take last played card instead of drawing a card
    private void takeLastPlayed(int card) {
        updateMessages(1, card);
        Card temp = playerHands[currentPlayerDisplay][card];
        playerHands[currentPlayerDisplay][card] = withdraw;
        playerHands[currentPlayerDisplay][card].setRevealed(false);
        withdraw = temp;
        withdraw.setRevealed(true);
        lastPlayed.setImageDrawable(temp.getImage());
        playerReady = false;
        cardDrew = false;
    }

    //Helper method that handles if a player decides to replace the drawn card with one of their own
    private void replaceWithNext(int card) {
        updateMessages(2, card);
        Card temp = playerHands[currentPlayerDisplay][card];
        withdraw = temp;
        withdraw.setRevealed(true);
        lastPlayed.setImageDrawable(temp.getImage());
        playerHands[currentPlayerDisplay][card] = next;
        playerHands[currentPlayerDisplay][card].setRevealed(false);
        if (card == 0) cardOneImageView.setImageDrawable(next.getImage());
        if (card == 1) cardTwoImageView.setImageDrawable(next.getImage());
        if (card == 2) cardThreeImageView.setImageDrawable(next.getImage());
        if (card == 3) cardFourImageView.setImageDrawable(next.getImage());
        if (card == 4) cardFiveImageView.setImageDrawable(next.getImage());
        if (card == 5) cardSixImageView.setImageDrawable(next.getImage());
        next = null;
        cardDrawn.setImageDrawable(null);
    }

    //Helper method that updates the image views so that they correctly display the current player's cards
    private void updateImageViews() {
        if (playerHands[currentPlayerDisplay][0] != null)
            cardOneImageView.setImageDrawable(playerHands[currentPlayerDisplay][0].getImage());
        if (playerHands[currentPlayerDisplay][1] != null)
            cardTwoImageView.setImageDrawable(playerHands[currentPlayerDisplay][1].getImage());
        if (playerHands[currentPlayerDisplay][2] != null)
            cardThreeImageView.setImageDrawable(playerHands[currentPlayerDisplay][2].getImage());
        if (playerHands[currentPlayerDisplay][3] != null)
            cardFourImageView.setImageDrawable(playerHands[currentPlayerDisplay][3].getImage());
        if (playerHands[currentPlayerDisplay][4] != null)
            cardFiveImageView.setImageDrawable(playerHands[currentPlayerDisplay][4].getImage());
        if (playerHands[currentPlayerDisplay][5] != null)
            cardSixImageView.setImageDrawable(playerHands[currentPlayerDisplay][5].getImage());
    }

    //Helper method that adds the player's message to the queue
    private void updateMessages(int key) {
        //Adds the completed message to the queue for the current player
        if (key == 0) {
            if (playerTurn == 1) messageQueue.offer(playerOneMessage);
            else if (playerTurn == 2) messageQueue.offer(playerTwoMessage);
            else if (playerTurn == 3) messageQueue.offer(playerThreeMessage);
            else if (playerTurn == 4) messageQueue.offer(playerFourMessage);
            else if (playerTurn == 5) messageQueue.offer(playerFiveMessage);
            else if (playerTurn == 6) messageQueue.offer(playerSixMessage);
            return;
        }
    }

    //Helper method that updates the player's message String based on what they did that turn
    private void updateMessages(int key, int card) {

        tempMessage = currentPlayerName.getText().toString() + ":";

        //If player takes last played
        if (key == 1) {
            tempMessage += " took the " + withdraw.getString() + " from the withdraw pile and replaced it with their " + cardPosition(card) + ".";
        }
        //If player take card drawn and replaces it with one of their own cards
        if (key == 2) {
            tempMessage += " took the card they drew and replaced it with their " + cardPosition(card) + ".";
        }
        //If player reveals their own card
        if (key == 4) {
            tempMessage += " looked at their " + cardPosition(card) + ".";
        }
    }

    //Helper method that updates the player's message String based on what they did that turn
    private void updateMessages(int key, String card) {
        tempMessage = currentPlayerName.getText().toString() + ":";
        //If player plays a seven or eight
        if (key == 3) {
            tempMessage += "played a " + card;
        }
    }

    //Helper method that returns the location of the given card
    private String cardPosition(int card) {
        if (card == 0) return "bottom left card";
        else if (card == 1) return "top left card";
        else if (card == 2) return "top right card";
        else if (card == 3) return "bottom right card";
        else if (card == 4) return "left penalty card";
        else return "right penalty card";
    }

    //Helper method that changes a player's message to a new one
    private void addMessage(String message) {
        if (playerTurn == 1) playerOneMessage = message;
        else if (playerTurn == 2) playerTwoMessage = message;
        else if (playerTurn == 3) playerThreeMessage = message;
        else if (playerTurn == 4) playerFourMessage = message;
        else if (playerTurn == 5) playerFiveMessage = message;
        else if (playerTurn == 6) playerSixMessage = message;
    }

    //Helper method that deep copies the messageQueue
    private Queue<SpannableString> deepCopyMessageQueue() {
        Queue<SpannableString> newQueue = new LinkedList<>();
        Iterator<String> messageQueueIterator = messageQueue.iterator();
        while (messageQueueIterator.hasNext())
            newQueue.offer(new SpannableString(messageQueueIterator.next()));
        return newQueue;
    }
}
