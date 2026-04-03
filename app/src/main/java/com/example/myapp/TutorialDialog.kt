package com.example.myapp

import android.app.AlertDialog
import android.content.Context

object TutorialDialog {

    fun show(context: Context) {
        GameStrings.init(context)
        showPage(context, 0)
    }

    private fun showPage(context: Context, index: Int) {
        val pages = GameStrings.tutorialPages()
        val (title, content) = pages[index]
        val builder = AlertDialog.Builder(context)
            .setTitle("$title (${index + 1}/${pages.size})")
            .setMessage(content.trimIndent())
        if (index > 0) {
            builder.setNeutralButton(GameStrings.tutBackBtn) { _, _ -> showPage(context, index - 1) }
        }
        if (index < pages.size - 1) {
            builder.setPositiveButton(GameStrings.tutNextBtn) { _, _ -> showPage(context, index + 1) }
        } else {
            builder.setPositiveButton(GameStrings.tutGotIt) { d, _ -> d.dismiss() }
        }
        builder.show()
    }
}
