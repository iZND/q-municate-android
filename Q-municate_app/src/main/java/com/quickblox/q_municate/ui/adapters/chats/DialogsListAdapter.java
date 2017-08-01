package com.quickblox.q_municate.ui.adapters.chats;

import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.databinding.ItemDialogBinding;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.adapters.base.BaseListAdapter;

import java.util.Iterator;
import java.util.List;

public class DialogsListAdapter extends BaseListAdapter<QBChatDialog> {

    private static final String TAG = DialogsListAdapter.class.getSimpleName();

    public DialogsListAdapter(BaseActivity baseActivity, List<QBChatDialog> objectsList) {
        super(baseActivity, objectsList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        QBChatDialog currentDialog = getItem(position);

        ItemDialogBinding binding;
        if (convertView == null) {
            binding = DataBindingUtil.inflate(layoutInflater,R.layout.item_dialog, parent, false);
        } else {
                // Recycling view
            binding = DataBindingUtil.getBinding(convertView);
        }

        binding.setDialog(currentDialog);
        binding.executePendingBindings();

        if (QBDialogType.PRIVATE.equals(currentDialog.getDialogType())) {
            displayGroupPhotoImage(currentDialog.getPhoto(), binding.avatarImageview);
        } else {
            binding.avatarImageview.setImageResource(R.drawable.placeholder_group);
            displayGroupPhotoImage(currentDialog.getPhoto(), binding.avatarImageview);
        }

        return binding.getRoot();
    }

   /* public void updateItem(DialogWrapper dlgWrapper) {
        Log.i(TAG, "updateItem = " + dlgWrapper.getChatDialog().getUnreadMessageCount());
        int position = -1;
        for (int i = 0; i < objectsList.size() ; i++) {
            DialogWrapper dialogWrapper  = objectsList.get(i);
            if (dialogWrapper.getChatDialog().getDialogId().equals(dlgWrapper.getChatDialog().getDialogId())){
                position = i;
                break;
            }
        }

        if (position != -1) {
            Log.i(TAG, "find position = " + position);
            objectsList.set(position, dlgWrapper);
        } else {
            addNewItem(dlgWrapper);
        }
    }

    public void updateItemPosition(DialogWrapper dlgWrapper) {
        if (!objectsList.get(0).equals(dlgWrapper)) {
            objectsList.remove(dlgWrapper);
            objectsList.add(0, dlgWrapper);
            notifyDataSetChanged();
        }
    }*/

    public void removeItem(String dialogId) {
        Iterator<QBChatDialog> iterator = objectsList.iterator();

        while (iterator.hasNext()){
            QBChatDialog dialogWrapper = iterator.next();
            if (dialogWrapper.getDialogId().equals(dialogId)){
                iterator.remove();
                notifyDataSetChanged();
                break;
            }
        }

    }

}