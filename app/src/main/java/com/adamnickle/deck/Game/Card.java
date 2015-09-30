package com.adamnickle.deck.Game;

import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.adamnickle.deck.R;

import java.io.IOException;


public class Card
{
    public static final int SUITS = 4;
    public static final int RANKS = 13;
    public static final int COUNT = SUITS * RANKS;

    public static final int SPADES = 0;
    public static final int HEARTS = 1;
    public static final int CLUBS = 2;
    public static final int DIAMONDS = 3;

    private static final int[][] CARD_RESOURCES = new int[][]
    {
        {
            R.drawable.ace_of_spades, R.drawable.two_of_spades, R.drawable.three_of_spades, R.drawable.four_of_spades,
            R.drawable.five_of_spades, R.drawable.six_of_spades, R.drawable.seven_of_spades, R.drawable.eight_of_spades,
            R.drawable.nine_of_spades, R.drawable.ten_of_spades, R.drawable.jack_of_spades2, R.drawable.queen_of_spades2, R.drawable.king_of_spades2
        },
        {
            R.drawable.ace_of_hearts, R.drawable.two_of_hearts, R.drawable.three_of_hearts, R.drawable.four_of_hearts,
            R.drawable.five_of_hearts, R.drawable.six_of_hearts, R.drawable.seven_of_hearts, R.drawable.eight_of_hearts,
            R.drawable.nine_of_hearts, R.drawable.ten_of_hearts, R.drawable.jack_of_hearts2, R.drawable.queen_of_hearts2, R.drawable.king_of_hearts2
        },
        {
            R.drawable.ace_of_clubs, R.drawable.two_of_clubs, R.drawable.three_of_clubs, R.drawable.four_of_clubs,
            R.drawable.five_of_clubs, R.drawable.six_of_clubs, R.drawable.seven_of_clubs, R.drawable.eight_of_clubs,
            R.drawable.nine_of_clubs, R.drawable.ten_of_clubs, R.drawable.jack_of_clubs2, R.drawable.queen_of_clubs2, R.drawable.king_of_clubs2
        },
        {
            R.drawable.ace_of_diamonds, R.drawable.two_of_diamonds, R.drawable.three_of_diamonds, R.drawable.four_of_diamonds,
            R.drawable.five_of_diamonds, R.drawable.six_of_diamonds, R.drawable.seven_of_diamonds, R.drawable.eight_of_diamonds,
            R.drawable.nine_of_diamonds, R.drawable.ten_of_diamonds, R.drawable.jack_of_diamonds2, R.drawable.queen_of_diamonds2, R.drawable.king_of_diamonds2
        }
    };

    private final int mSuit;
    private final int mRank;

    public Card( @IntRange( from=0, to=SUITS ) int suit, @IntRange( from=0, to=RANKS ) int rank )
    {
        mSuit = suit;
        mRank = rank;
    }

    public Card( @IntRange( from=0, to=COUNT ) int cardId )
    {
        this( cardId / RANKS, cardId % RANKS );
    }

    public Card()
    {
        this( (int)( Math.random() * COUNT ) );
    }

    public int getCardId()
    {
        return mSuit * RANKS + mRank;
    }

    public int getSuit()
    {
        return mSuit;
    }

    public int getRank()
    {
        return mRank;
    }

    public @DrawableRes int getResource()
    {
        return CARD_RESOURCES[ getSuit() ][ getRank() ];
    }

    public void writeToJson( JsonWriter writer ) throws IOException
    {
        writer.value( getCardId() );
    }

    public static Card readFromJson( JsonReader reader ) throws IOException
    {
        final int cardId = reader.nextInt();
        return new Card( cardId );
    }
}
