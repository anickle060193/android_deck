package com.adamnickle.deck;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EnumMap;

public class Message
{
    public enum Type
    {
        PlayerConnected,
        PlayerDisconnected
    }

    private enum Key
    {
        MessageType,
        Sender,
        PlayerName,
        PlayerAddress,
        DestinationAddress
    }

    private final EnumMap<Key, Object> mMessageMap = new EnumMap<>( Key.class );
    private boolean mIsValid = true;

    //region BLEH
    @SuppressWarnings( "unchecked" )
    private Message( byte[] data )
    {
        mIsValid = false;
        ObjectInputStream input = null;
        try
        {
            final ByteArrayInputStream byteInput = new ByteArrayInputStream( data );
            input = new ObjectInputStream( byteInput );
            mMessageMap.putAll( (EnumMap<Key, Object>)input.readObject() );
            mIsValid = true;
        }
        catch( IOException | ClassNotFoundException ex )
        {
            Deck.log( "An error occurred while de-serializing the message.", ex );
        }
        finally
        {
            if( input != null )
            {
                try
                {
                    input.close();
                }
                catch( IOException closingEx )
                {
                    Deck.log( "An error occurred while closing the message input stream.", closingEx );
                }
            }
        }
    }

    private Message( String senderAddress, Type type )
    {
        mMessageMap.put( Key.Sender, senderAddress );
        mMessageMap.put( Key.MessageType, type );
    }

    private Message set( Key key, Object value )
    {
        mMessageMap.put( key, value );
        return this;
    }

    public Message setDestination( String address )
    {
        return set( Key.DestinationAddress, address );
    }

    public boolean isValid()
    {
        return mIsValid;
    }

    public Type messageType()
    {
        return (Type)mMessageMap.get( Key.MessageType );
    }

    public String deviceName()
    {
        return (String)mMessageMap.get( Key.PlayerName );
    }

    public String deviceAddress()
    {
        return (String)mMessageMap.get( Key.PlayerAddress );
    }

    public boolean hasDestination()
    {
        return mMessageMap.containsKey( Key.DestinationAddress );
    }

    public String destination()
    {
        return (String)mMessageMap.get( Key.DestinationAddress );
    }

    public byte[] toBytes()
    {
        ObjectOutputStream output = null;
        try
        {
            final ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            output = new ObjectOutputStream( byteOutput );
            output.writeObject( this );
            output.flush();
            return byteOutput.toByteArray();
        }
        catch( IOException ex )
        {
            Deck.log( "An error occurred while serializing the message.", ex );
        }
        finally
        {
            if( output != null )
            {
                try
                {
                    output.close();
                }
                catch( IOException closingEx )
                {
                    Deck.log( "An error occurred while closing the message output stream.", closingEx );
                }
            }
        }
        return null;
    }

    public static Message fromBytes( byte[] data )
    {
        return new Message( data );
    }
    //endregion

    public static Message playerConnected( String deviceAddress, String playerName )
    {
        return new Message( deviceAddress, Type.PlayerConnected )
                .set( Key.PlayerName, playerName )
                .set( Key.PlayerAddress, deviceAddress );
    }

    public static Message playerDisconnected( String deviceAddress, String playerName )
    {
        return new Message( deviceAddress, Type.PlayerDisconnected )
                .set( Key.PlayerName, playerName )
                .set( Key.PlayerAddress, deviceAddress );
    }
}
