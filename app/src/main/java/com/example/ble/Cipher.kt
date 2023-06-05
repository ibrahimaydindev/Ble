package com.example.ble
import android.os.Build
import androidx.annotation.RequiresApi
import javax.crypto.Cipher
import java.security.spec.X509EncodedKeySpec
import java.security.*
import java.util.*

class Cipher {

    val publicKeyString = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnYJxbrXUFLCPbZkqJvKt\n" +
            "DmOrrB5Xq9SUKtNfQFyBJ8b55hzTMkacKTbOX0mvQmID2MnChcDdcdOrxnu4O7SM\n" +
            "5lUWj+imrdNcZgkm+4v+eFvSXwKtGByRo49j50gBhjVKi67cuxZAKFmfsatdSWWw\n" +
            "5r/2xt9v/AqvgCkUnyT1wjNSAFIkYwslsl++ss5Fbf6zkCblO6JGhy2YohKjBQU3\n" +
            "R48lnqLGNiVNRVfAl9N5Q3dDUR+OL9uW6AHv8KgUGFv/6VR/PH/kfUP1gy7zCz3p\n" +
            "BweM4jyWcMbPQb32NyXAGIW1MXogZU7Y3+gVNBgl5J+73Gh42MdSsSWekf2ZzTDe\n" +
            "5wIDAQAB\n"


    val privateKey = "MIICXAIBAAKBgQCDPjWIMx48xa/715VpvjZiVCuiF/oUj6qCYVE1KFIMKgsfClQf\n" +
            "6k74rC8vqNMB+jZPanQcRuDl55mN8qdMNX68pmATGpJHvzyFnuOtGwM98WSO6L7t\n" +
            "jCYA3h4HVSPefPC9jpyd+Srv3AKSAZ8PpxdJMjakEIpC+BYFkxbbMsgnSwIDAQAB\n" +
            "AoGAZrAwFodYq1hKYBTIVVp9Fuag1U1JYPkgAq++aIdJ2zaySPE97VLZw3yF1xaT\n" +
            "M0LhZ7X1b4KNyZUy8nvgJqLcq/qFxtTJ15IOHlpp2S33Cd1UfcAkkh1WwPMlnC2L\n" +
            "TVN4eMFJPB6hLB87WcjYR+LsA/y+Ndexi5EQh57DCjG/7EECQQDssnmk2W4MYrQ1\n" +
            "OuRqX8vy7ZR/m+1fIFzxs5kjcDAlnDIQ4MOdudqeB/SMSTWksIYl/oICxlvDDYcN\n" +
            "ZryuRR0xAkEAjfIrELFPYT8pXbdsdiBiw+6hZzK0mfjCLYsxpAAXG4/0CHj1NPve\n" +
            "J20LsH6yGqo5flWdfHg7bQHmnf9kxtH9OwJBAM8fDhsWuJnV9WNu+VmsIkedZgiU\n" +
            "ZY6MP0ixpBvCnB8NIzJpvENU0tzekTwBBBPs9DZjE1liQgHY4Ij1kb3ddMECQHXC\n" +
            "+o1/vM5+GzB/80DRP38z4739KC4xXc9xEn7wADvCov/AchZB+x2Ub0U+5z4OCWLR\n" +
            "XrWb/hlCoXRlJNN59W8CQCV7O37zl4O+ahVc+xU+aoRnL/h7aQSUtkG2ZsBLi43/\n" +
            "7fV8viVYDGkk7ZIbzuNncEvMLEvUKmOnDhMt3iegkPg=\n"

    val originalMessage="mustafa"
    val publicKey = convertToPublicKey(publicKeyString)
    val encryptedMessage = encrypt(originalMessage, publicKey)

    @RequiresApi(Build.VERSION_CODES.O)
    val encryptedMessageBase64 = Base64.getEncoder().encodeToString(encryptedMessage)


    fun convertToPublicKey(publicKeyString: String): PublicKey {
        val keyBytes = publicKeyString.toByteArray()
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    fun encrypt(message: String, publicKey: PublicKey): ByteArray {
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(message.toByteArray())
    }


}