package com.tunjid.androidbootstrap.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.DiffUtil
import com.tunjid.androidbootstrap.functions.collections.Lists
import com.tunjid.androidbootstrap.model.Tile
import com.tunjid.androidbootstrap.recyclerview.diff.Diff
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class ShiftingTileViewModel(application: Application) : AndroidViewModel(application) {

    val tiles: List<Tile> = ArrayList(generateTiles(NUM_TILES))
    private var changes: Boolean = false
    private val disposables: CompositeDisposable = CompositeDisposable()
    private val processor: PublishProcessor<DiffUtil.DiffResult> = PublishProcessor.create<DiffUtil.DiffResult>()

    init {
        dance()
    }

    fun toggleChanges() {
        changes = !changes
    }

    fun changes(): Boolean {
        return changes
    }

    fun watchTiles(): Flowable<DiffUtil.DiffResult> {
        return processor
    }

    private fun dance() {
        disposables.add(Flowable.interval(2, TimeUnit.SECONDS, Schedulers.io())
                .map { makeNewTiles() }
                .map { newTiles -> Diff.calculate(tiles, newTiles) { _, newTilesCopy -> newTilesCopy } }
                .observeOn(mainThread())
                .subscribe({ diff ->
                    Lists.replace(tiles, diff.items)
                    processor.onNext(diff.result)
                }, processor::onError))
    }

    private fun generateTiles(number: Int): List<Tile> {
        val result = ArrayList<Tile>(number)

        for (i in 0 until number) result.add(Tile.generate(i))
        result.shuffle()

        return result
    }

    private fun makeNewTiles(): List<Tile> {
        return generateTiles(if (changes) Math.max(5, (Math.random() * NUM_TILES).toInt()) else NUM_TILES)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        processor.onComplete()
    }

    companion object {

        private const val NUM_TILES = 32
    }
}
