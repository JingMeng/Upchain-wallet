package pro.upchain.wallet.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import pro.upchain.wallet.domain.ETHWallet;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.wallet.DeterministicSeed;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

/**
 * 浠ュお鍧婇挶鍖呭垱寤哄伐鍏风被
 */

public class ETHWalletUtils {

    private static ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    /**
     * 闅忔満
     */
    private static final SecureRandom secureRandom = SecureRandomUtils.secureRandom();
    private Credentials credentials;

    /**
     * 閫氱敤鐨勪互澶潑鍩轰簬bip44鍗忚鐨勫姪璁拌瘝璺緞 锛坕mtoken jaxx Metamask myetherwallet锛?
     */
    public static String ETH_JAXX_TYPE = "m/44'/60'/0'/0/0";
    public static String ETH_LEDGER_TYPE = "m/44'/60'/0'/0";
    public static String ETH_CUSTOM_TYPE = "m/44'/60'/1'/0/0";

    /**
     * 鍒涘缓鍔╄璇嶏紝骞堕€氳繃鍔╄璇嶅垱寤洪挶鍖?
     *
     * @param walletName
     * @param pwd
     * @return
     */
    public static ETHWallet generateMnemonic(String walletName, String pwd) {
        String[] pathArray = ETH_JAXX_TYPE.split("/");
        String passphrase = "";
        long creationTimeSeconds = System.currentTimeMillis() / 1000;

        DeterministicSeed ds = new DeterministicSeed(secureRandom, 128, passphrase, creationTimeSeconds);
        return generateWalletByMnemonic(walletName, ds, pathArray, pwd);
    }

    /**
     * 閫氳繃瀵煎叆鍔╄璇嶏紝瀵煎叆閽卞寘
     *
     * @param path 璺緞
     * @param list 鍔╄璇?
     * @param pwd  瀵嗙爜
     * @return
     */
    public static ETHWallet importMnemonic(String path, List<String> list, String pwd) {
        if (!path.startsWith("m") && !path.startsWith("M")) {
            //鍙傛暟闈炴硶
            return null;
        }
        String[] pathArray = path.split("/");
        if (pathArray.length <= 1) {
            //鍐呭涓嶅
            return null;
        }
        String passphrase = "";
        long creationTimeSeconds = System.currentTimeMillis() / 1000;
        DeterministicSeed ds = new DeterministicSeed(list, null, passphrase, creationTimeSeconds);
        return generateWalletByMnemonic(generateNewWalletName(), ds, pathArray, pwd);
    }

    @NonNull
    private static String generateNewWalletName() {
        char letter1 = (char) (int) (Math.random() * 26 + 97);
        char letter2 = (char) (int) (Math.random() * 26 + 97);

        String walletName = String.valueOf(letter1) + String.valueOf(letter2) + "-新钱包";
        while (WalletDaoUtils.walletNameChecking(walletName)) {
            letter1 = (char) (int) (Math.random() * 26 + 97);
            letter2 = (char) (int) (Math.random() * 26 + 97);
            walletName = String.valueOf(letter1) + String.valueOf(letter2) + "-新钱包";
        }
        return walletName;
    }

    /**
     * @param walletName 閽卞寘鍚嶇О
     * @param ds         鍔╄璇嶅姞瀵嗙瀛?
     * @param pathArray  鍔╄璇嶆爣鍑?
     * @param pwd        瀵嗙爜
     * @return
     */
    @Nullable
    public static ETHWallet generateWalletByMnemonic(String walletName, DeterministicSeed ds,
                                                     String[] pathArray, String pwd) {
        //绉嶅瓙
        byte[] seedBytes = ds.getSeedBytes();
//        System.out.println(Arrays.toString(seedBytes));
        //鍔╄璇?
        List<String> mnemonic = ds.getMnemonicCode();
        LogUtils.d("mnemonic: " + mnemonic);
        if (seedBytes == null)
            return null;

        DeterministicKey dkKey = HDKeyDerivation.createMasterPrivateKey(seedBytes);

        for (int i = 1; i < pathArray.length; i++) {
            ChildNumber childNumber;
            if (pathArray[i].endsWith("'")) {
                int number = Integer.parseInt(pathArray[i].substring(0,
                        pathArray[i].length() - 1));
                childNumber = new ChildNumber(number, true);
            } else {
                int number = Integer.parseInt(pathArray[i]);
                childNumber = new ChildNumber(number, false);
            }
            dkKey = HDKeyDerivation.deriveChildKey(dkKey, childNumber);
        }

        ECKeyPair keyPair = ECKeyPair.create(dkKey.getPrivKeyBytes());
        ETHWallet ethWallet = generateWallet(walletName, pwd, keyPair);
        if (ethWallet != null) {
            ethWallet.setMnemonic(convertMnemonicList(mnemonic));
        }
        return ethWallet;
    }

    private static String convertMnemonicList(List<String> mnemonics) {
        StringBuilder sb = new StringBuilder();
        int size =  mnemonics.size();

        for (int i = 0; i < size; i++) {
            sb.append(mnemonics.get(i));
            if (i != size - 1) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    @Nullable
    private static ETHWallet generateWallet(String walletName, String pwd, ECKeyPair ecKeyPair) {
        WalletFile keyStoreFile;
        try {
            keyStoreFile = Wallet.create(pwd, ecKeyPair, 1024, 1); // WalletUtils. .generateNewWalletFile();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        BigInteger publicKey = ecKeyPair.getPublicKey();
        String s = publicKey.toString();
        LogUtils.i("ETHWalletUtils", "publicKey = " + s);

        String wallet_dir = AppFilePath.Wallet_DIR;
        LogUtils.i("ETHWalletUtils", "wallet_dir = " + wallet_dir);

        String keystorePath = "keystore_" + walletName + ".json";
        File destination = new File(wallet_dir, "keystore_" + walletName + ".json");

        //鐩綍涓嶅瓨鍦ㄥ垯鍒涘缓鐩綍锛屽垱寤轰笉浜嗗垯鎶ラ敊
        if (!createParentDir(destination)) {
            return null;
        }
        try {
            objectMapper.writeValue(destination, keyStoreFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        ETHWallet ethWallet = new ETHWallet();
        ethWallet.setName(walletName);
        ethWallet.setAddress(Keys.toChecksumAddress(keyStoreFile.getAddress()));
        ethWallet.setKeystorePath(destination.getAbsolutePath());
        ethWallet.setPassword(Md5Utils.md5(pwd));
        return ethWallet;
    }

    /**
     * 閫氳繃keystore.json鏂囦欢瀵煎叆閽卞寘
     *
     * @param keystore 鍘焜son鏂囦欢
     * @param pwd      json鏂囦欢瀵嗙爜
     * @return
     */
    public static ETHWallet loadWalletByKeystore(String keystore, String pwd) {
        Credentials credentials = null;
        try {
            WalletFile walletFile = null;
            walletFile = objectMapper.readValue(keystore, WalletFile.class);

//            WalletFile walletFile = new Gson().fromJson(keystore, WalletFile.class);
            credentials = Credentials.create(Wallet.decrypt(pwd, walletFile));
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e("ETHWalletUtils", e.toString());
        } catch (CipherException e) {
            LogUtils.e("ETHWalletUtils", e.toString());
//            ToastUtils.showToast(R.string.load_wallet_by_official_wallet_keystore_input_tip);
            e.printStackTrace();
        }
        if (credentials != null) {
            return generateWallet(generateNewWalletName(), pwd, credentials.getEcKeyPair());
        }
        return null;
    }

    /**
     * 閫氳繃鏄庢枃绉侀挜瀵煎叆閽卞寘
     *
     * @param privateKey
     * @param pwd
     * @return
     */
    public static ETHWallet loadWalletByPrivateKey(String privateKey, String pwd) {
        Credentials credentials = null;
        ECKeyPair ecKeyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));
        return generateWallet(generateNewWalletName(), pwd, ecKeyPair);
    }


    public static boolean isTooSimplePrivateKey(String privateKey) {

        if (Numeric.toBigInt(privateKey).intValue() < 100000000) {
            return true;
        } else {
            return true;
        }

    }

    private static boolean createParentDir(File file) {
        //鍒ゆ柇鐩爣鏂囦欢鎵€鍦ㄧ殑鐩綍鏄惁瀛樺湪
        if (!file.getParentFile().exists()) {
            //濡傛灉鐩爣鏂囦欢鎵€鍦ㄧ殑鐩綍涓嶅瓨鍦紝鍒欏垱寤虹埗鐩綍
            System.out.println("目标文件所在目录不存在，准备创建");
            if (!file.getParentFile().mkdirs()) {
                System.out.println("鍒涘缓鐩爣鏂囦欢鎵€鍦ㄧ洰褰曞け璐ワ紒");
                return false;
            }
        }
        return true;
    }

    /**
     * 淇敼閽卞寘瀵嗙爜
     *
     * @param walletId
     * @param walletName
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public static ETHWallet modifyPassword(long walletId, String walletName, String oldPassword, String newPassword) {
        ETHWallet ethWallet = WalletDaoUtils.ethWalletDao.load(walletId);
        Credentials credentials = null;
        ECKeyPair keypair = null;
        try {
            credentials = WalletUtils.loadCredentials(oldPassword, ethWallet.getKeystorePath());
            keypair = credentials.getEcKeyPair();
            File destinationDirectory = new File(AppFilePath.Wallet_DIR, "keystore_" + walletName + ".json");
            WalletUtils.generateWalletFile(newPassword, keypair, destinationDirectory, true);
            ethWallet.setPassword(newPassword);
            WalletDaoUtils.ethWalletDao.insert(ethWallet);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ethWallet;
    }

    /**
     * 瀵煎嚭鏄庢枃绉侀挜
     *
     * @param walletId 閽卞寘Id
     * @param pwd      閽卞寘瀵嗙爜
     * @return
     */
    public static String derivePrivateKey(long walletId, String pwd) {
        ETHWallet ethWallet = WalletDaoUtils.ethWalletDao.load(walletId);
        Credentials credentials;
        ECKeyPair keypair;
        String privateKey = null;
        try {
            credentials = WalletUtils.loadCredentials(pwd, ethWallet.getKeystorePath());
            keypair = credentials.getEcKeyPair();
            privateKey = Numeric.toHexStringNoPrefixZeroPadded(keypair.getPrivateKey(), Keys.PRIVATE_KEY_LENGTH_IN_HEX);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    /**
     * 瀵煎嚭keystore鏂囦欢
     *
     * @param walletId
     * @param pwd
     * @return
     */
    public static String deriveKeystore(long walletId, String pwd) {
        ETHWallet ethWallet = WalletDaoUtils.ethWalletDao.load(walletId);
        String keystore = null;
        WalletFile walletFile;
        try {
            walletFile = objectMapper.readValue(new File(ethWallet.getKeystorePath()), WalletFile.class);
            keystore = objectMapper.writeValueAsString(walletFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keystore;
    }

    /**
     * 鍒犻櫎閽卞寘
     *
     * @param walletId
     * @return
     */
    public static boolean deleteWallet(long walletId) {
        ETHWallet ethWallet = WalletDaoUtils.ethWalletDao.load(walletId);
        if (deleteFile(ethWallet.getKeystorePath())) {
            WalletDaoUtils.ethWalletDao.deleteByKey(walletId);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 鍒犻櫎鍗曚釜鏂囦欢
     *
     * @param fileName 瑕佸垹闄ょ殑鏂囦欢鐨勬枃浠跺悕
     * @return 鍗曚釜鏂囦欢鍒犻櫎鎴愬姛杩斿洖true锛屽惁鍒欒繑鍥瀎alse
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 濡傛灉鏂囦欢璺緞鎵€瀵瑰簲鐨勬枃浠跺瓨鍦紝骞朵笖鏄竴涓枃浠讹紝鍒欑洿鎺ュ垹闄?
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
//                System.out.println("鍒犻櫎鍗曚釜鏂囦欢" + fileName + "鎴愬姛锛?);
                return true;
            } else {
//                System.out.println("鍒犻櫎鍗曚釜鏂囦欢" + fileName + "澶辫触锛?);
                return false;
            }
        } else {
//            System.out.println("鍒犻櫎鍗曚釜鏂囦欢澶辫触锛? + fileName + "涓嶅瓨鍦紒");
            return false;
        }
    }
}

