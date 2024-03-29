package com.adamnickle.deck;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.EditText;


public final class Dialog
{
    private Dialog() { }

    public interface OnConfirmationListener
    {
        void onOK();
        void onCancel();
    }

    public static void showConfirmation( Context context, final String title, final String message, final String positiveButtonText, final String negativeButtonText, final OnConfirmationListener listener )
    {
        new AlertDialog.Builder( context )
                .setTitle( title )
                .setMessage( message )
                .setPositiveButton( positiveButtonText, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        dialog.dismiss();
                    }
                } )
                .setNegativeButton( negativeButtonText, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        dialog.cancel();
                    }
                } )
                .setOnDismissListener( new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss( DialogInterface dialog )
                    {
                        listener.onOK();
                    }
                } )
                .setOnCancelListener( new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel( DialogInterface dialog )
                    {
                        listener.onCancel();
                    }
                } )
                .show();
    }

    public static void showAlert( Context context, final String title, final String message )
    {
        new AlertDialog.Builder( context )
                .setTitle( title )
                .setMessage( message )
                .setPositiveButton( "OK", null )
                .show();
    }

    public interface OnTextDialogClickListener
    {
        void onClick( DialogInterface dialog, String text );
    }

    public static void showTextDialog( Context context, String title, boolean cancelable, String positiveText, final OnTextDialogClickListener listener )
    {
        final EditText editText = (EditText)LayoutInflater.from( context ).inflate( R.layout.edit_text, null );

        final AlertDialog.Builder builder = new AlertDialog.Builder( context )
                .setTitle( title )
                .setView( editText )
                .setPositiveButton( positiveText, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        listener.onClick( dialog, editText.getText().toString() );
                    }
                } );

        if( cancelable )
        {
            builder.setNegativeButton( "Cancel", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                    dialog.cancel();
                }
            } );
        }

        builder.show();
    }

    public interface OnSingleChoiceDialogClickListener<T>
    {
        void onClick( DialogInterface dialog, T obj, int which );
    }

    public static <T> void showSingleChoiceDialog( Context context, String title, boolean cancelable, T[] objects, final OnSingleChoiceDialogClickListener<T> listener )
    {
        final ArrayAdapter<T> adapter = new ArrayAdapter<T>( context, android.R.layout.select_dialog_item, objects );
        final AlertDialog.Builder builder = new AlertDialog.Builder( context )
                .setTitle( title )
                .setAdapter( adapter, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        listener.onClick( dialog, adapter.getItem( which ), which );
                        dialog.dismiss();
                    }
                } );
        if( cancelable )
        {
            builder.setNegativeButton( "Cancel", null );
        }
        builder.show();
    }
}
