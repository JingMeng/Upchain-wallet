package pro.upchain.wallet.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;

import pro.upchain.wallet.R;


public class MnemonicBackupAlertDialog extends Dialog implements View.OnClickListener {


    public MnemonicBackupAlertDialog(@NonNull Context context) {
        super(context);
    }

    public MnemonicBackupAlertDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_mnemonic_backup_alert);
        setCanceledOnTouchOutside(false);
        //鍒濆鍖栫晫闈㈡帶浠剁殑浜嬩欢
        initEvent();
    }

    private void initEvent() {
        findViewById(R.id.btn_confirm).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:// 纭畾
                dismiss();
                break;
        }
    }

}

