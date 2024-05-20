package amar.das.acbook.googlesigninauthentication;

import android.app.Activity;
import android.widget.Toast;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import java.security.SecureRandom;
import java.util.Base64;

import amar.das.acbook.sharedpreferences.SharedPreferencesHelper;

public class GoogleIdOptionsUtil {
    Activity context;
    private static final String WEB_CLIENT_ID = "335367595409-btviu8514bhj0bllae76hs8crab3e436.apps.googleusercontent.com";//we need two client id android and webapplication client id and we are using Web application" client ID.In the context of OAuth (Open Authorization), an OAuth Client ID, also sometimes referred to as a Client ID or Consumer Key, serves two main purposes:1. Client Identification:It acts as a unique identifier for your application during the OAuth authorization process.It allows the authorization server (e.g., Google, Facebook) to verify that the sign-in request originates from your legitimate application and not a malicious source attempting to steal user information.2. Authorization:When a user attempts to sign in with a provider like Google using OAuth, the authorization server needs to verify that your app is authorized to access their account information.The Client ID helps establish this trust relationship between your app and the authorization server.

    public GoogleIdOptionsUtil(Activity context) {
        this.context = context;
    }

    public void createGoogleIdOption() {
//        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()//GetSignInWithGoogleOption
//                .setFilterByAuthorizedAccounts(false)//First,check if the user has any accounts that have previously been used to sign in to your app by calling the API with the setFilterByAuthorizedAccounts parameter set to true. Users can choose between available accounts to sign in. If no authorized Google Accounts are available, the user should be prompted to sign up with any of their available accounts. To do this, prompt the user by calling the API again and setting setFilterByAuthorizedAccounts to false
//                .setServerClientId(WEB_CLIENT_ID)// purpose: Client Identification: The WEB_CLIENT_ID acts as a unique identifier for your application during the Google Sign-In process. It allows Google to verify that the sign-in request originates from your legitimate application and not a malicious source. Authorization: When a user attempts to sign in with Google, Google needs to verify that your app is authorized to access their Google account information. The WEB_CLIENT_ID helps establish this trust relationship between your app and Google. Security: Google uses the WEB_CLIENT_ID to restrict access to sensitive user data. It ensures that only your authorized app can access the user's Google ID token and other information retrieved during the sign-in process. Client Restriction: By using different WEB_CLIENT_IDs for different applications (web, mobile, etc.), you can control which applications can access a user's Google account information.
//                .setAutoSelectEnabled(true)//This enables automatic account selection. If the user has only one Google account on the device, it will be automatically selected without needing them to choose.
//                .setNonce(nonceString)//This helps prevent security attacks like replay attacks. Make sure nonceString is a unique value generated for each sign-in attempt.
//                .build();

        GetSignInWithGoogleOption signInWithGoogleOption = new GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
                .setNonce(generateNonce()) // Replace with your actual nonce string
                .build();

        createCredentialRequest(signInWithGoogleOption);
    }

    private void createCredentialRequest(GetSignInWithGoogleOption signInWithGoogleOption) {
        GetCredentialRequest getCredentialRequest = getCredentialRequest(signInWithGoogleOption);
        CredentialManager credentialManager = CredentialManager.create(context);

        credentialManager.getCredentialAsync(//This method asynchronously retrieves credentials based on a getCredentialRequest
                context, // Use activity based context to avoid undefined
                getCredentialRequest,
                null,//You can leave this argument as null unless you specifically need to be able to cancel the credential retrieval process mid-operation.
                ((Activity) context).getMainExecutor(),//This executor is specifically designed to schedule tasks for execution on the main thread of the activity.
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {

                        if (result.getCredential() != null) {// Check if credentials are available
                            handleSignIn(result); // User is already signed in
                        } else {
                            handleNotSignedInUser(); // User needs to sign in
                        }
                    }
                    @Override
                    public void onError(GetCredentialException e) {
                        handleFailure(e);
                    }
                }
        );
    }

    private void handleNotSignedInUser() {
//        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()//GetSignInWithGoogleOption
//                .setFilterByAuthorizedAccounts(false)//First,check if the user has any accounts that have previously been used to sign in to your app by calling the API with the setFilterByAuthorizedAccounts parameter set to true. Users can choose between available accounts to sign in. If no authorized Google Accounts are available, the user should be prompted to sign up with any of their available accounts. To do this, prompt the user by calling the API again and setting setFilterByAuthorizedAccounts to false
//                .setServerClientId(WEB_CLIENT_ID)// purpose: Client Identification: The WEB_CLIENT_ID acts as a unique identifier for your application during the Google Sign-In process. It allows Google to verify that the sign-in request originates from your legitimate application and not a malicious source. Authorization: When a user attempts to sign in with Google, Google needs to verify that your app is authorized to access their Google account information. The WEB_CLIENT_ID helps establish this trust relationship between your app and Google. Security: Google uses the WEB_CLIENT_ID to restrict access to sensitive user data. It ensures that only your authorized app can access the user's Google ID token and other information retrieved during the sign-in process. Client Restriction: By using different WEB_CLIENT_IDs for different applications (web, mobile, etc.), you can control which applications can access a user's Google account information.
//                .build();
        Toast.makeText(context, "Please sign in with Google", Toast.LENGTH_LONG).show();
        destroySignInActivity(context);
        // Handle the case where the user is not signed in (e.g., show a sign-in button)
    }

    private void handleSignIn(GetCredentialResponse result) {
        Credential credential = result.getCredential();// Handle the successfully returned credential.
        CustomCredential customCredential = (CustomCredential) credential;
        if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {//GoogleIdTokenCredential is a class that represents a credential obtained through Google Sign-In
            try {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(customCredential.getData());
                 //System.out.println(googleIdTokenCredential.getId()+"  "+googleIdTokenCredential.getIdToken()+" "+googleIdTokenCredential.getDisplayName()+" "+googleIdTokenCredential.getFamilyName() +" "+googleIdTokenCredential.getGivenName()+" "+googleIdTokenCredential.getPhoneNumber());
                SharedPreferencesHelper.setString(context,SharedPreferencesHelper.Keys.GOOGLE_SIGNIN_EMAIL.name(),googleIdTokenCredential.getId());
                Toast.makeText(context, "SUCCESSFUL SIGNIN: "+SharedPreferencesHelper.getString(context,SharedPreferencesHelper.Keys.GOOGLE_SIGNIN_EMAIL.name(),null), Toast.LENGTH_LONG).show();
                //goToNavigationActivity(context);

                // Use googleIdTokenCredential and extract id to validate and
                // authenticate on your server.
                //  } catch (GoogleIdTokenParsingException e) {
            } catch (Exception e) {
                System.out.println(e+"Received an invalid google id token response");
                Toast.makeText(context, "FAILED TO SIGNIN", Toast.LENGTH_LONG).show();
//              goToNavigationActivity(context);
            }
        }else{ // Catch any unrecognized custom credential type here.
            Toast.makeText(context, "Unexpected type of credential", Toast.LENGTH_LONG).show();
        }
        destroySignInActivity(context);
    }
    private void handleFailure(GetCredentialException e) { // Handle the failure
        System.out.println(e + "GetCredentialException occurred");
        Toast.makeText(context, "FAILED TO SIGNIN", Toast.LENGTH_LONG).show();
        destroySignInActivity(context);
        //Toast.makeText(context, "FAILED: GetCredentialException occurred", Toast.LENGTH_LONG).show();
    }

    private String generateNonce() {//Nonce Purpose: The nonce is used to link the ID token to a specific user session, ensuring that the token cannot be reused by an attacker. It helps in verifying that the response to the authentication request was not replayed by a malicious user.
        SecureRandom secureRandom = new SecureRandom();
        byte[] nonceBytes = new byte[16]; // 16 bytes is 128 bits
        secureRandom.nextBytes(nonceBytes);
        return Base64.getEncoder().encodeToString(nonceBytes);     // Encode the nonce in Base64
    }
    public static void destroySignInActivity(Activity context) {
        context.finish();
//        Intent intent = new Intent(context, NavigationActivity.class);
//        context.startActivity(intent);
    }
    private GetCredentialRequest getCredentialRequest(GetSignInWithGoogleOption signInWithGoogleOption){
        return new GetCredentialRequest.Builder()//GetCredentialRequest It’s used to retrieve saved credentials (such as passwords) for your app
                .addCredentialOption(signInWithGoogleOption)//Adds a credential option (in this case, googleIdOption) to the request
                .build();
    }
}
//public class GoogleIdOptionsUtil {
//    Context context;
//    private static final String WEB_CLIENT_ID = "335367595409-btviu8514bhj0bllae76hs8crab3e436.apps.googleusercontent.com";//we need two client id android and webapplication client id and we are using Web application" client ID.In the context of OAuth (Open Authorization), an OAuth Client ID, also sometimes referred to as a Client ID or Consumer Key, serves two main purposes:1. Client Identification:It acts as a unique identifier for your application during the OAuth authorization process.It allows the authorization server (e.g., Google, Facebook) to verify that the sign-in request originates from your legitimate application and not a malicious source attempting to steal user information.2. Authorization:When a user attempts to sign in with a provider like Google using OAuth, the authorization server needs to verify that your app is authorized to access their account information.The Client ID helps establish this trust relationship between your app and the authorization server.
//    public GoogleIdOptionsUtil(Context context){
//       this.context=context;
//    }
//    public void createGoogleIdOption() {
//        String nonceString = generateNonce();//Replace with your actual nonce string
////        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()//GetSignInWithGoogleOption
////                .setFilterByAuthorizedAccounts(false)//First,check if the user has any accounts that have previously been used to sign in to your app by calling the API with the setFilterByAuthorizedAccounts parameter set to true. Users can choose between available accounts to sign in. If no authorized Google Accounts are available, the user should be prompted to sign up with any of their available accounts. To do this, prompt the user by calling the API again and setting setFilterByAuthorizedAccounts to false
////                .setServerClientId(WEB_CLIENT_ID)// purpose: Client Identification: The WEB_CLIENT_ID acts as a unique identifier for your application during the Google Sign-In process. It allows Google to verify that the sign-in request originates from your legitimate application and not a malicious source. Authorization: When a user attempts to sign in with Google, Google needs to verify that your app is authorized to access their Google account information. The WEB_CLIENT_ID helps establish this trust relationship between your app and Google. Security: Google uses the WEB_CLIENT_ID to restrict access to sensitive user data. It ensures that only your authorized app can access the user's Google ID token and other information retrieved during the sign-in process. Client Restriction: By using different WEB_CLIENT_IDs for different applications (web, mobile, etc.), you can control which applications can access a user's Google account information.
////                .setAutoSelectEnabled(true)//This enables automatic account selection. If the user has only one Google account on the device, it will be automatically selected without needing them to choose.
////                .setNonce(nonceString)//This helps prevent security attacks like replay attacks. Make sure nonceString is a unique value generated for each sign-in attempt.
////                .build();
//
//        GetSignInWithGoogleOption signInWithGoogleOption = new GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
//                .setNonce(nonceString) // Replace with your actual nonce string
//                .build();
//
//        createCredentialRequest(signInWithGoogleOption);
//    }
//    public void createCredentialRequest(GetSignInWithGoogleOption signInWithGoogleOption) {
//        GetCredentialRequest getCredentialRequest = new GetCredentialRequest.Builder()//GetCredentialRequest It’s used to retrieve saved credentials (such as passwords) for your app
//                .addCredentialOption(signInWithGoogleOption)//Adds a credential option (in this case, googleIdOption) to the request
//                .build();
//
//         CredentialManager credentialManager = CredentialManager.create(context);
//         credentialManager.getCredentialAsync(//This method asynchronously retrieves credentials based on a getCredentialRequest
//                context, // Use activity based context to avoid undefined
//                 getCredentialRequest,
//                null,//You can leave this argument as null unless you specifically need to be able to cancel the credential retrieval process mid-operation.
//                  ((Activity) context).getMainExecutor(),//This executor is specifically designed to schedule tasks for execution on the main thread of the activity.
//                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
//                    @Override
//                    public void onResult(GetCredentialResponse result){
//
//                        if (result.getCredential() != null){// Check if credentials are available
//                            handleSignIn(result); // User is already signed in
//                        } else {
//                            handleNotSignedInUser(); // User needs to sign in
//                        }
//                    }
//                    @Override
//                    public void onError(GetCredentialException e){
//                        handleFailure(e);
//                    }
//                }
//              );
//    }
//    private void handleNotSignedInUser() {
////        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()//GetSignInWithGoogleOption
////                .setFilterByAuthorizedAccounts(false)//First,check if the user has any accounts that have previously been used to sign in to your app by calling the API with the setFilterByAuthorizedAccounts parameter set to true. Users can choose between available accounts to sign in. If no authorized Google Accounts are available, the user should be prompted to sign up with any of their available accounts. To do this, prompt the user by calling the API again and setting setFilterByAuthorizedAccounts to false
////                .setServerClientId(WEB_CLIENT_ID)// purpose: Client Identification: The WEB_CLIENT_ID acts as a unique identifier for your application during the Google Sign-In process. It allows Google to verify that the sign-in request originates from your legitimate application and not a malicious source. Authorization: When a user attempts to sign in with Google, Google needs to verify that your app is authorized to access their Google account information. The WEB_CLIENT_ID helps establish this trust relationship between your app and Google. Security: Google uses the WEB_CLIENT_ID to restrict access to sensitive user data. It ensures that only your authorized app can access the user's Google ID token and other information retrieved during the sign-in process. Client Restriction: By using different WEB_CLIENT_IDs for different applications (web, mobile, etc.), you can control which applications can access a user's Google account information.
////                .build();
//        Toast.makeText(context, "Please sign in with Google", Toast.LENGTH_LONG).show();
//        // Handle the case where the user is not signed in (e.g., show a sign-in button)
//    }
//    private void handleSignIn(GetCredentialResponse result) {
//        Credential credential = result.getCredential();// Handle the successfully returned credential.
//        CustomCredential customCredential = (CustomCredential) credential;
//        if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {//GoogleIdTokenCredential is a class that represents a credential obtained through Google Sign-In
//            try {
//                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(customCredential.getData());
//                Toast.makeText(context, ""+googleIdTokenCredential.getId(), Toast.LENGTH_SHORT).show();
//
//
//                // Use googleIdTokenCredential and extract id to validate and
//                // authenticate on your server.
//                //  } catch (GoogleIdTokenParsingException e) {
//            } catch (Exception e) {
//                // Log.e(TAG, "Received an invalid google id token response", e);
//                System.out.println(e );
//                Toast.makeText(context, "Received an invalid google id token response"+e, Toast.LENGTH_SHORT).show();
//            }
//        }else{
//            // Catch any unrecognized custom credential type here.
//            Toast.makeText(context, "Unexpected type of credential", Toast.LENGTH_LONG).show();
//        }
//
//
////        // Handle the successfully returned credential.
////        Credential credential = result.getCredential();
////
////        if (credential instanceof PublicKeyCredential) {
////            // Share responseJson such as a GetCredentialResponse on your server to
////            // validate and authenticate
////            String responseJson = ((PublicKeyCredential) credential).getAuthenticationResponseJson();
////            Toast.makeText(context, "json: "+responseJson, Toast.LENGTH_SHORT).show();
////
////            // Handle responseJson...
////        } else if (credential instanceof PasswordCredential) {
////            // Send ID and password to your server to validate and authenticate.
////            String username = ((PasswordCredential) credential).getId();
////            String password = ((PasswordCredential) credential).getPassword();
////            Toast.makeText(context, "username:"+username+"  password:"+password, Toast.LENGTH_SHORT).show();
////            // Handle username and password...
////        } else if (credential instanceof CustomCredential) {
////            CustomCredential customCredential = (CustomCredential) credential;
////            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {//GoogleIdTokenCredential is a class that represents a credential obtained through Google Sign-In
////                try {
////                    GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(customCredential.getData());
////                    Toast.makeText(context, ""+googleIdTokenCredential.getId(), Toast.LENGTH_SHORT).show();
////                    // Use googleIdTokenCredential and extract id to validate and
////                    // authenticate on your server.
////              //  } catch (GoogleIdTokenParsingException e) {
////                } catch (Exception e) {
////                   // Log.e(TAG, "Received an invalid google id token response", e);
////                    System.out.println(e );
////                    Toast.makeText(context, "Received an invalid google id token response"+e, Toast.LENGTH_SHORT).show();
////                }
////            } else {
////                // Catch any unrecognized custom credential type here.
////                Toast.makeText(context, "Unexpected type of credential", Toast.LENGTH_SHORT).show();
////            }
////        } else {
////            // Catch any unrecognized credential type here.
////            //Log.e(TAG, "Unexpected type of credential");
////            Toast.makeText(context, "Unexpected type of credential", Toast.LENGTH_SHORT).show();
////        }
//    }
//
//    private void handleFailure(GetCredentialException e) {
//        // Handle the failure
//      //  Log.e(TAG, "GetCredentialException occurred", e);
//        System.out.println(e+"GetCredentialException occurred");
//        Toast.makeText(context, "Signing Failed", Toast.LENGTH_LONG).show();
//        //Toast.makeText(context, "FAILED: GetCredentialException occurred", Toast.LENGTH_LONG).show();
//    }
//    private String generateNonce() {//Nonce Purpose: The nonce is used to link the ID token to a specific user session, ensuring that the token cannot be reused by an attacker. It helps in verifying that the response to the authentication request was not replayed by a malicious user.
//        SecureRandom secureRandom = new SecureRandom();
//        byte[] nonceBytes = new byte[16]; // 16 bytes is 128 bits
//        secureRandom.nextBytes(nonceBytes);
//        return Base64.getEncoder().encodeToString(nonceBytes);     // Encode the nonce in Base64
//    }
//}
