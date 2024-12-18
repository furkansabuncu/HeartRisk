import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

object GoogleSignInHelper {

    private lateinit var googleSignInClient: GoogleSignInClient

    // Google Sign-In yapılandırmasını bir kez yapıyoruz
    fun configureSignIn(context: Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()  // Kullanıcının e-posta adresini almak için
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    // Sign-In işlemini başlatıyoruz
    fun signIn(activity: Activity, requestCode: Int) {
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, requestCode)
    }

    // Sonuçları işliyoruz
    fun handleSignInResult(requestCode: Int, resultCode: Int, data: Intent?, onSuccess: (GoogleSignInAccount) -> Unit, onFailure: () -> Unit) {
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    onSuccess(account)  // Giriş başarılı
                }
            } catch (e: ApiException) {
                onFailure()  // Giriş başarısız
            }
        }
    }

    // Firebase ile Google hesabıyla giriş yapmak için
    fun firebaseSignIn(account: GoogleSignInAccount, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()  // Firebase ile giriş başarılı
                } else {
                    onFailure()  // Firebase ile giriş başarısız
                }
            }
    }

    // Sign-Out işlemi
    fun signOut() {
        googleSignInClient.signOut()
    }

    private const val RC_SIGN_IN = 9001
}
