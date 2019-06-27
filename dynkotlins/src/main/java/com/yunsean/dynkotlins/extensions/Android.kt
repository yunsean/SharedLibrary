package com.yunsean.dynkotlins.extensions

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Handler
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.dylan.common.application.Application
import com.dylan.common.utils.Utility
import com.dylan.uiparts.views.ToastEx
import com.yunsean.dynkotlins.R
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textColor
import org.json.JSONObject
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

fun Context?.isTabletDevice(): Boolean = if (this == null) false else this.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
fun Context?.screenWidth(): Int = if (this == null) 0 else this.resources.displayMetrics.widthPixels
fun Context?.screenHeight(): Int = if (this == null) 0 else this.resources.displayMetrics.heightPixels

interface OnSettingDialogListener {
    fun onSettingDialog(dialog: Dialog, contentView: View)
}
interface OnDialogItemClickedListener {
    fun onClick(dialog: Dialog, contentView: View, clickedView: View)
}
interface OnDismissListener {
    fun onDismiss()
}
fun Context.createDialog(dialogResId: Int, setting: ((dialog: Dialog, contentView: View)-> Unit)? = null, clickableResId: IntArray? = null, clicked: ((dialog: Dialog, contentView: View, clickedView: View)-> Unit)? = null, dismiss: OnDismissListener? = null, dismissDelay: Int = 0, dialogWidth: Int = 0, styleResId: Int = R.style.CenterDialog, cancelable: Boolean = false): Dialog {
    var dialogWidth = dialogWidth
    val view = LayoutInflater.from(this).inflate(dialogResId, null)
    val dialog = Dialog(this, styleResId)
    setting?.let{ it(dialog, view) }
    if (clicked != null && clickableResId != null) {
        for (i in clickableResId) view.findViewById<View>(i).setOnClickListener { v -> clicked(dialog, view, v) }
    }
    dialog.setTitle(null)
    dialog.setCancelable(true)
    if (dismiss != null) dialog.setOnDismissListener { dismiss.onDismiss() }
    dialog.setContentView(view)
    dialogWidth = if (dialogWidth != 0) dialogWidth else if (this.isTabletDevice()) this.screenWidth() * 4 / 10 else this.screenWidth() * 8 / 10
    val window = dialog.window
    val wl = window!!.attributes
    wl.width = dialogWidth
    window.setWindowAnimations(R.style.DialogBottomAnimate)
    dialog.onWindowAttributesChanged(wl)
    if (dismissDelay > 0) Handler().postDelayed({ dialog.dismiss() }, dismissDelay.toLong())
    dialog.setCancelable(cancelable)
    return dialog
}
fun Context.createDialog(dialogResId: Int, setting: OnSettingDialogListener? = null, clickableResId: IntArray? = null, clicked: OnDialogItemClickedListener? = null, dismiss: OnDismissListener? = null, dismissDelay: Int = 0, dialogWidth: Int = 0, styleResId: Int = R.style.CenterDialog, cancelable: Boolean = false): Dialog {
    var dialogWidth = dialogWidth
    val view = LayoutInflater.from(this).inflate(dialogResId, null)
    val dialog = Dialog(this, styleResId)
    if (setting != null) setting.onSettingDialog(dialog, view)
    if (clicked != null && clickableResId != null) {
        for (i in clickableResId) view.findViewById<View>(i).setOnClickListener { v -> clicked.onClick(dialog, view, v) }
    }
    dialog.setTitle(null)
    dialog.setCancelable(true)
    if (dismiss != null) dialog.setOnDismissListener { dismiss.onDismiss() }
    dialog.setContentView(view)
    dialogWidth = if (dialogWidth != 0) dialogWidth else if (this.isTabletDevice()) this.screenWidth() * 4 / 10 else this.screenWidth() * 8 / 10
    val window = dialog.window
    val wl = window!!.attributes
    wl.width = dialogWidth
    window.setWindowAnimations(R.style.DialogBottomAnimate)
    dialog.onWindowAttributesChanged(wl)
    if (dismissDelay > 0) Handler().postDelayed({ dialog.dismiss() }, dismissDelay.toLong())
    dialog.setCancelable(cancelable)
    return dialog
}
fun Context.showDialog(dialogResId: Int, setting: OnSettingDialogListener? = null, clickableResId: IntArray? = null, clicked: OnDialogItemClickedListener? = null, dismiss: OnDismissListener? = null, dismissDelay: Int = 0, dialogWidth: Int = 0, styleResId: Int = R.style.CenterDialog, cancelable: Boolean = false): Dialog {
    val dialog = this.createDialog(dialogResId, setting, clickableResId, clicked, dismiss, dismissDelay, dialogWidth, styleResId, cancelable)
    dialog.show()
    return dialog
}
fun Context.showDialog(dialogResId: Int, setting: ((dialog: Dialog, contentView: View)-> Unit)? = null, clickableResId: IntArray? = null, clicked: ((dialog: Dialog, contentView: View, clickedView: View)-> Unit)? = null, dismiss: OnDismissListener? = null, dismissDelay: Int = 0, dialogWidth: Int = 0, styleResId: Int = R.style.CenterDialog, cancelable: Boolean = false): Dialog {
    val dialog = this.createDialog(dialogResId, setting, clickableResId, clicked, dismiss, dismissDelay, dialogWidth, styleResId, cancelable)
    dialog.show()
    return dialog
}

fun Context.createBottomDialog(dialogResId: Int, setting: ((dialog: Dialog, contentView: View)-> Unit)?, clickableResId: IntArray? = null, clicked: ((dialog: Dialog, contentView: View, clickedView: View)-> Unit)? = null, dialogWidth: Int = ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT, cancelable: Boolean = false): Dialog {
    val view = LayoutInflater.from(this).inflate(dialogResId, null)
    val dialog = Dialog(this, com.dylan.uiparts.R.style.BottomDialog)
    setting?.let { it(dialog, view) }
    dialog.setTitle(null)
    dialog.setContentView(view)
    dialog.setCancelable(true)
    if (clicked != null) {
        clickableResId?.forEach { view.findViewById<View>(it)?.setOnClickListener { v -> clicked(dialog, view, v) } }
    }
    val window = dialog.window
    val wl = window!!.attributes
    wl.x = 0
    wl.y = Utility.getScreenHeight(this)
    wl.width = dialogWidth
    wl.height = dialogHeight
    window.setWindowAnimations(com.dylan.uiparts.R.style.DialogBottomAnimate)
    dialog.onWindowAttributesChanged(wl)
    dialog.setCancelable(cancelable)
    return dialog
}
fun Context.createBottomDialog(dialogResId: Int, setting: OnSettingDialogListener?, clickableResId: IntArray? = null, clicked: OnDialogItemClickedListener? = null, dialogWidth: Int = ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT, cancelable: Boolean = false): Dialog {
    val view = LayoutInflater.from(this).inflate(dialogResId, null)
    val dialog = Dialog(this, com.dylan.uiparts.R.style.BottomDialog)
    setting?.onSettingDialog(dialog, view)
    dialog.setTitle(null)
    dialog.setContentView(view)
    dialog.setCancelable(true)
    if (clicked != null) {
        clickableResId?.forEach { view.findViewById<View>(it)?.setOnClickListener { v -> clicked.onClick(dialog, view, v) } }
    }
    val window = dialog.window
    val wl = window!!.attributes
    wl.x = 0
    wl.y = Utility.getScreenHeight(this)
    wl.width = dialogWidth
    wl.height = dialogHeight
    window.setWindowAnimations(com.dylan.uiparts.R.style.DialogBottomAnimate)
    dialog.onWindowAttributesChanged(wl)
    dialog.setCancelable(cancelable)
    return dialog
}
fun Context.showBottomDialog(dialogResId: Int, setting: OnSettingDialogListener?, clickableResId: IntArray? = null, clicked: OnDialogItemClickedListener? = null, dialogWidth: Int = ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT, cancelable: Boolean = false) {
    this.createBottomDialog(dialogResId, setting, clickableResId, clicked, dialogWidth, dialogHeight, cancelable).show()
}
fun Context.showBottomDialog(dialogResId: Int, setting: ((dialog: Dialog, contentView: View)-> Unit)?, clickableResId: IntArray? = null, clicked: ((dialog: Dialog, contentView: View, clickedView: View)-> Unit)? = null, dialogWidth: Int = ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT, cancelable: Boolean = false) {
    this.createBottomDialog(dialogResId, setting, clickableResId, clicked, dialogWidth, dialogHeight, cancelable).show()
}

fun Context.showMessage(title: String, msg: String, btn1: String = "OK", listener1: DialogInterface.OnClickListener? = null, btn2: String? = null, listener2: DialogInterface.OnClickListener? = null) {
    AlertDialog.Builder(this)
            .setTitle(title)
            .setCancelable(false)
            .setMessage(msg)
            .setPositiveButton(btn1, listener1)
            .setNegativeButton(btn2, listener2)
            .show()
}

fun View?.set_tag(tag: Any?): View? {
    if (this != null) this.tag = tag
    return this
}
fun View?.set_tag(resId: Int, tag: Any?): View? {
    this?.findViewById<View>(resId)?.tag = tag
    return this
}
fun Activity?.set_tag(resId: Int, tag: Any?): Activity? {
    this?.findViewById<View>(resId).set_tag(tag)
    return this
}
fun View?.set_tag(tag: Any?, vararg args: Int): View? {
    args.forEach { this?.findViewById<View>(it)?.tag = tag }
    return this
}
fun Activity?.set_tag(tag: Any?, vararg args: Int): Activity? {
    args.forEach { this?.findViewById<View>(it)?.tag = tag }
    return this
}
fun set_tag(tag: Any?, vararg args: View?) {
    args.forEach { it?.tag = tag }
}

fun View?.set_click(callback: View.OnClickListener) {
    this?.onClick {callback.onClick(it)}
}
fun View?.set_click(callback: () -> Unit, vararg args: Int) {
    args.forEach { this?.findViewById<View>(it)?.let { it.onClick {callback()} } }
}
fun Activity?.set_click(callback: () -> Unit, vararg args: Int) {
    args.forEach { this?.findViewById<View>(it)?.let { it.onClick {callback()} } }
}

fun View?.set_text(text: String?): View? {
    if (this != null && this is TextView && text != null) (this as TextView).text = text
    return this
}
fun View?.set_text(resId: Int, text: String?): View? {
    this?.findViewById<View>(resId).set_text(text)
    return this
}
fun Activity?.set_text(resId: Int, text: String?): Activity? {
    this?.findViewById<View>(resId).set_text(text)
    return this
}

fun View?.set_topDrawable(bitmap: Bitmap?): View? {
    if (this != null && this is TextView && bitmap != null) {
        val drawables = this.compoundDrawables
        (this as TextView).setCompoundDrawables(drawables[0], BitmapDrawable(bitmap), drawables[2], drawables[3])
    }
    return this
}
fun View?.set_topDrawable(resId: Int, bitmap: Bitmap?): View? {
    this?.findViewById<View>(resId).set_topDrawable(bitmap)
    return this
}
fun Activity?.set_topDrawable(resId: Int, bitmap: Bitmap?): Activity? {
    this?.findViewById<View>(resId).set_topDrawable(bitmap)
    return this
}

fun View?.set_text(json: JSONObject?, name: String, fallback: String? = null): View? {
    this.set_text(json?.optString(name, fallback))
    return this
}
fun View?.set_text(resId: Int, json: JSONObject?, name: String, fallback: String? = null): View? {
    this?.set_text(resId, json?.optString(name, fallback))
    return this
}
fun Activity?.set_text(resId: Int, json: JSONObject?, name: String, fallback: String? = null): Activity? {
    this?.set_text(resId, json?.optString(name, fallback))
    return this
}

fun View?.set_image(@DrawableRes resId: Int): View? {
    if (this != null && this is ImageView && resId != 0) (this as ImageView).imageResource = resId
    return this
}
fun View?.set_image(@IdRes resId: Int, @DrawableRes imageResId: Int): View? {
    this?.findViewById<View>(resId).set_image(imageResId)
    return this
}
fun Activity?.set_image(@IdRes resId: Int, @DrawableRes imageResId: Int): Activity? {
    this?.findViewById<View>(resId).set_image(imageResId)
    return this
}

fun View?.set_textColor(color: Int): View? {
    if (this != null && this is TextView) this.textColor = color
    return this
}
fun View?.set_textColor(color: Int, vararg args: Int): View? {
    args.forEach { this?.findViewById<View>(it)?.set_textColor(color) }
    return this
}
fun Activity?.set_textColor(color: Int, vararg args: Int): Activity? {
    args.forEach { this?.findViewById<View>(it)?.set_textColor(color) }
    return this
}
fun set_textColor(color: Int, vararg args: View?) {
    args.forEach { it?.set_textColor(color) }
}

fun View?.set_visible(visible: Boolean): View? {
    if (this != null) this.visibility = if (visible) View.VISIBLE else View.GONE
    return this
}
fun View?.set_visible(visible: Boolean, vararg args: Int): View? {
    args.forEach { this?.findViewById<View>(it).set_visible(visible) }
    return this
}
fun Activity?.set_visible(visible: Boolean, vararg args: Int): Activity? {
    args.forEach { this?.findViewById<View>(it).set_visible(visible) }
    return this
}
fun View?.set_visible(visible: Int): View? {
    if (this != null) this.visibility = visible
    return this
}
fun View?.set_visible(visible: Int, vararg args: Int): View? {
    args.forEach { this?.findViewById<View>(it).set_visible(visible) }
    return this
}
fun Activity?.set_visible(visible: Int, vararg args: Int): Activity? {
    args.forEach { this?.findViewById<View>(it).set_visible(visible) }
    return this
}
fun set_visible(visible: Boolean, vararg args: View?) {
    args.forEach { it?.set_visible(visible) }
}
fun set_visible(visible: Int, vararg args: View?) {
    args.forEach { it?.set_visible(visible) }
}

fun Context.toastex(message: String?, duration: Int = Toast.LENGTH_SHORT) {
    ToastEx.makeText(this, message, duration).show()
}

fun Context?.savePref(key: String, value: Any?, prefsName: String = "DEFAULT") {
    if (this != null) {
        val settings = getSharedPreferences(prefsName, 0)
        val editor = settings.edit()
        editor.putString(key, value?.toString())
        editor.commit()
    }
}
fun Context?.savePref(keys: Array<String>, values: Array<out Any?>, prefsName: String = "DEFAULT") {
    if (this != null) {
        val settings = getSharedPreferences(prefsName, 0)
        val editor = settings.edit()
        for (i in keys.indices) {
            editor.putString(keys[i], values[i]?.toString())
        }
        editor.commit()
    }
}
fun Context?.readPref(keys: Array<String>, prefsName: String = "DEFAULT") : Array<String?> {
    val results = arrayOfNulls<String>(keys.size)
    if (this != null) {
        val settings = getSharedPreferences(prefsName, 0)
        for (i in keys.indices) {
            results[i] = settings.getString(keys[i], "")
        }
    }
    return results
}
fun Context?.readPref(key: String, prefsName: String = "DEFAULT") : String? {
    if (this != null) {
        val settings = getSharedPreferences(prefsName, 0)
        return settings.getString(key, null)
    } else {
        return null
    }
}
fun <T: Any> Context?.readPref(key: String, defaultValue: T, prefsName: String = "DEFAULT") : T? {
    if (this != null) {
        val settings = getSharedPreferences(prefsName, 0)
        val value = settings.getString(key, null) ?: return defaultValue
        try {
            if (defaultValue.javaClass.isAssignableFrom(String::class.java)) return value as T
            val valueOf = defaultValue.javaClass.getMethod("valueOf", *arrayOf<Class<*>>(String::class.java))
            if (valueOf != null) {
                return valueOf!!.invoke(null, *arrayOf<Any>(value)) as T
            }
        } catch (ex: Exception) {
        }
    }
    return defaultValue
}
fun <T> T?.loge(tag: String = "dylan") : T? {
    if (this != null) Log.e(tag, this.toString())
    return this
}
fun <T> T?.logi(tag: String = "dylan") : T? {
    if (this != null) Log.i(tag, this.toString())
    return this
}
fun <T> T?.logw(tag: String = "dylan") : T? {
    if (this != null) Log.w(tag, this.toString())
    return this
}
fun loges(message: String?, tag: String = "dylan") {
    Log.e(tag, message)
}
fun logws(message: String?, tag: String = "dylan") {
    Log.w(tag, message)
}
fun logis(message: String?, tag: String = "dylan") {
    Log.i(tag, message)
}

fun <T> loget(ret: T, message: String?, tag: String = "dylan"): T {
    Log.e(tag, message)
    return ret
}
fun <T> logwt(ret: T, message: String?, tag: String = "dylan"): T {
    Log.w(tag, message)
    return ret
}
fun <T> logit(ret: T, message: String?, tag: String = "dylan"): T {
    Log.i(tag, message)
    return ret
}

fun Context.activity(clazz : Class<*>) {
    var intent = Intent(this, clazz)
    this.startActivity(intent)
}
fun Activity.activity(clazz : Class<*>, requestCode : Int) {
    var intent = Intent(this, clazz)
    this.startActivityForResult(intent, requestCode)
}
fun Context.intent(clazz: Class<*>) : Intent = Intent(this, clazz)
fun Intent.start(context: Context) {
    context.startActivity(this)
}
fun Intent.start(fragment: android.support.v4.app.Fragment) {
    fragment.startActivity(this)
}
fun Intent.flag(flags: Int): Intent {
    this.flags = flags
    return this
}
fun Intent.start(context: Activity, requestCode: Int) {
    context.startActivityForResult(this, requestCode)
}

fun Throwable?.toastex() {
    Application.context()?.let {
        ToastEx.makeText(Application.context(), this?.localizedMessage, Toast.LENGTH_SHORT).show()
    }
    this?.printStackTrace()
}

fun Type?.isSubClassOf(clazz: Type) : Boolean {
    var rawType : Type? = if (this is ParameterizedType) this.rawType else this
    return if (rawType == null || clazz == null) false
    else if (rawType is Class<*> && clazz is Class<*>) {
        return clazz.isAssignableFrom(rawType)
    } else {
        return false
    }
}
fun Type?.rawType() : Type? {
    return if (this is ParameterizedType) this.rawType else this
}
fun Type?.actualType(index : Int) : Type? {
    return if (this is ParameterizedType && this.actualTypeArguments.size > index) this.actualTypeArguments[index] else null
}
fun Type?.ownerType() : Type? {
    return if (this is ParameterizedType) this.ownerType else null
}

fun Array<String?>?.hasNullOrBlank(): Boolean = this?.let { return this.indexOfFirst { it?.let { return@indexOfFirst it.trim().length < 1 } ?: true } >= 0 } ?: true
fun String?.base64(): String? = if (this != null) Base64.encodeToString(toByteArray(), Base64.DEFAULT) else null
fun String?.md5(): String? {
    if (this == null) return null
    val messagedigest = MessageDigest.getInstance("MD5")
    messagedigest.update(this.toByteArray())
    return messagedigest.digest().toHex()
}

fun TextView?.onChanged(entry : ((Editable?) -> Unit)?) : TextView? {
    this?.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            entry?.let { entry(p0) }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }
    })
    return this
}

fun Date?.kdate(): String? = if (this != null) SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(this) else null
fun Date?.ktime(): String? = if (this != null) SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(this) else null
fun Date?.kdateTime(format: String = "yyyy-MM-dd HH:mm:ss"): String? = if (this != null) SimpleDateFormat(format, Locale.CHINA).format(this) else null
fun Date?.age(): Int? {
    return this?.let {
        val cal = Calendar.getInstance()
        if (cal.before(it)) return 0
        val yearNow = cal.get(Calendar.YEAR)
        val monthNow = cal.get(Calendar.MONTH)
        val dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH)
        cal.time = it
        val yearBirth = cal.get(Calendar.YEAR)
        val monthBirth = cal.get(Calendar.MONTH)
        val dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH)
        var age = yearNow - yearBirth
        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) age--
            } else {
                age--
            }
        }
        return age
    }
}

fun String?.kdate(): Date? = try { if (this != null) SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(this) else null } catch (_: Exception) { null }
fun String?.ktime(): Date? = try { if (this != null) SimpleDateFormat("HH:mm:ss", Locale.CHINA).parse(this) else null } catch (_: Exception) { null }
fun String?.kdateTime(format: String = "yyyy-MM-dd HH:mm:ss"): Date? = try { if (this != null) SimpleDateFormat(format, Locale.CHINA).parse(this) else null } catch (_: Exception) { null }

fun Calendar?.kdate(): String? = this?.time.kdate()
fun Calendar?.ktime(): String? = this?.time.ktime()
fun Calendar?.kdateTime(format: String = "yyyy-MM-dd HH:mm:ss"): String? = this?.time.kdateTime(format)
fun Calendar.beginOfDay(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    return calendar
}
fun Calendar.endOfDay(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    return calendar
}
fun Calendar.setBeginOfDay() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
}
fun Calendar.setEndOfDay() {
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
}
fun Calendar.setTime(hour: Int, minute: Int, second: Int = 0) {
    set(Calendar.HOUR_OF_DAY, hour)
    set(Calendar.MINUTE, minute)
    set(Calendar.SECOND, second)
}

fun Long?.kdate(): String? = if (this != null) Date(this).kdate() else null
fun Long?.ktime(): String? = if (this != null) Date(this).ktime() else null
fun Long?.kdateTime(format: String = "yyyy-MM-dd HH:mm:ss"): String? = if (this != null) Date(this).kdateTime(format) else null

fun TextView?.text(): String = this?.text?.toString() ?: ""

fun Activity.extraInt(name: String, fallback: Int = 0): Int = this.intent.getIntExtra(name, fallback)
fun Activity.extraLong(name: String, fallback: Long = 0): Long = this.intent.getLongExtra(name, fallback)
fun Activity.extraText(name: String, fallback: String? = null): String? = this.intent.getStringExtra(name)

fun Context.dip2px(dipValue: Float): Int = (dipValue * getResources().getDisplayMetrics().density + 0.5f).toInt()
fun Context.px2dip(pxValue: Float): Int = (pxValue / getResources().getDisplayMetrics().density + 0.5f).toInt()
fun Context.px2sp(pxValue: Float): Int = (pxValue / getResources().getDisplayMetrics().scaledDensity + 0.5f).toInt()
fun Context.sp2px(spValue: Float): Int = (spValue * getResources().getDisplayMetrics().scaledDensity + 0.5f).toInt()

fun Context.getCompactColor(resId: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) resources.getColor(resId, null)
    else resources.getColor(resId)
}

private val HEX_CHARS = "0123456789ABCDEF".toCharArray()
fun ByteArray?.toHex() : String? {
    if (this == null) return null
    val result = StringBuffer()
    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(HEX_CHARS[firstIndex])
        result.append(HEX_CHARS[secondIndex])
    }
    return result.toString()
}
fun String?.asHex() : ByteArray? {
    if (this == null) return null
    val result = ByteArray(length / 2)
    for (i in 0 until length step 2) {
        val firstIndex = HEX_CHARS.indexOf(this[i]);
        val secondIndex = HEX_CHARS.indexOf(this[i + 1]);
        val octet = firstIndex.shl(4).or(secondIndex)
        result.set(i.shr(1), octet.toByte())
    }
    return result
}
