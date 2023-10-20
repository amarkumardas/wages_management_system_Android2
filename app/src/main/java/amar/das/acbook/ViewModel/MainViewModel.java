package amar.das.acbook.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    MutableLiveData<String> mutableLiveData=new MutableLiveData<>();

    //create set text method
    public void setText(String data){
        mutableLiveData.setValue(data);
    }

    public MutableLiveData<String> getText(){
        return mutableLiveData;
    }
}
//    In Android Studio, MutableLiveData is a class provided by the Android Architecture Components library, which is a part of the Android Jetpack library. It is commonly used to hold and observe data in a way that's compatible with the Android app's lifecycle. MutableLiveData is a subclass of the LiveData class and is designed to hold and emit data that can be changed or updated over time.
//
//        The key characteristic of MutableLiveData is that it can be modified (or mutated) using its setValue or postValue methods. These methods allow you to update the data held by the MutableLiveData instance and automatically notify any registered observers when the data changes.
//
//        Here's a brief explanation of the two main methods used with MutableLiveData:
//
//        setValue(T value): This method should be called on the main thread and is used to set a new value for the MutableLiveData. It immediately dispatches the change to any active observers.
//
//        postValue(T value): This method is safe to be called from background threads. It posts the new value to the main thread's message queue, ensuring that the change is made on the main thread. It is often used when you need to update the MutableLiveData from a background thread.