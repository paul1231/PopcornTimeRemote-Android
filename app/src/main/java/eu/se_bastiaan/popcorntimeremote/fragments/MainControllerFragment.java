package eu.se_bastiaan.popcorntimeremote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import com.koushikdutta.async.future.FutureCallback;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.activities.ControllerActivity;
import eu.se_bastiaan.popcorntimeremote.rpc.PopcornTimeRpcClient;
import eu.se_bastiaan.popcorntimeremote.utils.LogUtils;
import eu.se_bastiaan.popcorntimeremote.widget.ClearableEditText;
import eu.se_bastiaan.popcorntimeremote.widget.JoystickView;

public class MainControllerFragment extends Fragment {

    @InjectView(R.id.joystick)
    JoystickView joystickView;
    @InjectView(R.id.searchButton)
    ImageButton searchButton;
    @InjectView(R.id.favouriteButton)
    ImageButton favouriteButton;
    @InjectView(R.id.backButton)
    ImageButton backButton;
    @InjectView(R.id.tabsButton)
    ImageButton tabsButton;
    @InjectView(R.id.searchInput)
    ClearableEditText searchInput;

    private View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.searchButton:
                    if(searchInput.getVisibility() == View.VISIBLE) {
                        mClearableEditTextListener.didClearText();
                    } else {
                        searchInput.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.backButton:
                    getClient().back(mResponseListener);
                    break;
                case R.id.favouriteButton:
                    getClient().toggleFavourite(mResponseListener);
                    break;
                case R.id.tabsButton:
                    getClient().toggleTabs(mResponseListener);
                    break;
            }
        }
    };

    private JoystickView.OnJoystickMoveListener mOnJoystickMoveListener = new JoystickView.OnJoystickMoveListener() {
        @Override
        public void onValueChanged(int angle, int power, JoystickView.Direction direction) {
            LogUtils.d("mOnJoystickMoveListener", power);

            switch (direction) {
                case CENTER:
                    getClient().enter(mResponseListener);
                    break;
                case UP:
                    getClient().up(mResponseListener);
                    break;
                case DOWN:
                    getClient().down(mResponseListener);
                    break;
                case RIGHT:
                    getClient().right(mResponseListener);
                    break;
                case LEFT:
                    getClient().left(mResponseListener);
                    break;
            }
        }
    };

    private ClearableEditText.Listener mClearableEditTextListener = new ClearableEditText.Listener() {
        @Override
        public void didClearText() {
            searchInput.setVisibility(View.INVISIBLE);
            getClient().clearSearch(mResponseListener);
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(searchInput.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    };

    private TextWatcher mClearableEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.toString().equals("")) {
                getClient().clearSearch(mResponseListener);
                return;
            }
            getClient().filterSearch(s.toString(), mResponseListener);
        }
    };

    private FutureCallback<PopcornTimeRpcClient.RpcResponse> mResponseListener = new FutureCallback<PopcornTimeRpcClient.RpcResponse>() {
        @Override
        public void onCompleted(Exception e, PopcornTimeRpcClient.RpcResponse result) {
            if(result != null && e != null) {
                LogUtils.d("MainControllerFragment", result.result);
            } else if(e != null) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtils.d("JoyStickMainControllerFragment", "onCreateView");

        View v = inflater.inflate(R.layout.fragment_maincontroller, container, false);
        ButterKnife.inject(this, v);

        searchButton.setOnClickListener(mButtonClickListener);
        favouriteButton.setOnClickListener(mButtonClickListener);
        backButton.setOnClickListener(mButtonClickListener);
        tabsButton.setOnClickListener(mButtonClickListener);

        joystickView.setOnJoystickMoveListener(mOnJoystickMoveListener);
        joystickView.setJoystickImage(JoystickView.Direction.CENTER, R.drawable.ic_action_ok);
        joystickView.setJoystickImage(JoystickView.Direction.RIGHT, R.drawable.ic_action_right);
        joystickView.setJoystickImage(JoystickView.Direction.LEFT, R.drawable.ic_action_left);
        joystickView.setJoystickImage(JoystickView.Direction.UP, R.drawable.ic_action_up);
        joystickView.setJoystickImage(JoystickView.Direction.DOWN, R.drawable.ic_action_down);

        searchInput.setIconAlwaysVisible(true);
        searchInput.setListener(mClearableEditTextListener);
        searchInput.addTextChangedListener(mClearableEditTextWatcher);

        return v;
    }

    private PopcornTimeRpcClient getClient() {
        try {
            return ((ControllerActivity) getActivity()).getClient();
        } catch (Exception e) {}
        return new PopcornTimeRpcClient(getActivity(), "0.0.0.0", "8008", "", "");
    }

}