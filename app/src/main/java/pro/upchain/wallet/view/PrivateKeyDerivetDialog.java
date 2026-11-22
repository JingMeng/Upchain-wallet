package pro.upchain.wallet.view;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import pro.upchain.wallet.R;


public class PrivateKeyDerivetDialog extends Dialog implements View.OnClickListener {


    private TextView tvPrivateKey;
    private TextView btnCopy;

    public void setPrivateKey(String privateKey) {
        tvPrivateKey.setText(privateKey);
    }

    private String privateKey;

    public PrivateKeyDerivetDialog(@NonNull Context context) {
        super(context);
    }

    public PrivateKeyDerivetDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_derive_private_key);
        setCanceledOnTouchOutside(false);
        tvPrivateKey = (TextView)findViewById(R.id.tv_private_key);
        btnCopy = (TextView) findViewById(R.id.btn_copy);
        //鍒濆鍖栫晫闈㈡帶浠剁殑浜嬩欢
        initEvent();
    }

    private void initEvent() {
        btnCopy.setOnClickListener(this);
        findViewById(R.id.lly_close).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_copy:// 纭畾
                ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                // 灏嗘枃鏈唴瀹规斁鍒扮郴缁熷壀璐存澘閲屻€?
                if (cm != null) {
                    // 鍒涘缓鏅€氬瓧绗﹀瀷ClipData
                    ClipData mClipData = ClipData.newPlainText("Label", tvPrivateKey.getText().toString());
                    // 灏咰lipData鍐呭鏀惧埌绯荤粺鍓创鏉块噷銆?
                    cm.setPrimaryClip(mClipData);
                }
                btnCopy.setText(R.string.derive_private_key_already_copy_btn);
                break;
            case R.id.lly_close:
                dismiss();
                break;
        }
    }

}

