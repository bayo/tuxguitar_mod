/*
 * Created on 23-nov-2005
 */
package org.herac.tuxguitar.song.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.herac.tuxguitar.song.factory.TGFactory;

/**
 * @author julian
 */
public class TGSong implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String artist;
	private String album;
	private String author;
	private String date;
	private String copyright;
	private String writer;
	private String transcriber;
	private String comments;
	private final List<TGTrack> tracks;
	private final List<TGMeasureHeader> measureHeaders;
	private final List<TGChannel> channels;

	public TGSong() {
		this.name = new String();
		this.artist = new String();
		this.album = new String();
		this.author = new String();
		this.date = new String();
		this.copyright = new String();
		this.writer = new String();
		this.transcriber = new String();
		this.comments = new String();
		this.tracks = new ArrayList<TGTrack>();
		this.channels = new ArrayList<TGChannel>();
		this.measureHeaders = new ArrayList<TGMeasureHeader>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlbum() {
		return this.album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getArtist() {
		return this.artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getDate() {
		return this.date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCopyright() {
		return this.copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getWriter() {
		return this.writer;
	}

	public void setWriter(String writer) {
		this.writer = writer;
	}

	public String getTranscriber() {
		return this.transcriber;
	}

	public void setTranscriber(String transcriber) {
		this.transcriber = transcriber;
	}

	public String getComments() {
		return this.comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public int countMeasureHeaders(){
		return this.measureHeaders.size();
	}

	public void addMeasureHeader(TGMeasureHeader measureHeader){
		this.addMeasureHeader(countMeasureHeaders(),measureHeader);
	}

	public void addMeasureHeader(int index,TGMeasureHeader measureHeader){
		measureHeader.setSong(this);
		this.measureHeaders.add(index,measureHeader);
	}

	public void removeMeasureHeader(int index){
		this.measureHeaders.remove(index);
	}

	public void removeMeasureHeader(TGMeasureHeader measureHeader){
		this.measureHeaders.remove(measureHeader);
	}

	public TGMeasureHeader getMeasureHeader(int index){
		return this.measureHeaders.get(index);
	}

	public Iterator<TGMeasureHeader> getMeasureHeaders() {
		return this.measureHeaders.iterator();
	}

	public int countTracks(){
		return this.tracks.size();
	}

	public void addTrack(TGTrack track){
		this.addTrack(countTracks(),track);
	}

	public void addTrack(int index,TGTrack track){
		track.setSong(this);
		this.tracks.add(index,track);
	}

	public void moveTrack(int index,TGTrack track){
		this.tracks.remove(track);
		this.tracks.add(index,track);
	}

	public void removeTrack(TGTrack track){
		this.tracks.remove(track);
		track.clear();
	}

	public TGTrack getTrack(int index){
		return this.tracks.get(index);
	}

	public Iterator<TGTrack> getTracks() {
		return this.tracks.iterator();
	}

	public int countChannels(){
		return this.channels.size();
	}

	public void addChannel(TGChannel channel){
		this.addChannel(countChannels(),channel);
	}

	public void addChannel(int index,TGChannel channel){
		this.channels.add(index,channel);
	}

	public void moveChannel(int index,TGChannel channel){
		this.channels.remove(channel);
		this.channels.add(index,channel);
	}

	public void removeChannel(TGChannel channel){
		this.channels.remove(channel);
	}

	public TGChannel getChannel(int index){
		return this.channels.get(index);
	}

	public Iterator<TGChannel> getChannels() {
		return this.channels.iterator();
	}

	public boolean isEmpty(){
		return (countMeasureHeaders() == 0 || countTracks() == 0);
	}

	public void clear(){
		Iterator<TGTrack> tracks = getTracks();
		while(tracks.hasNext()){
			TGTrack track = tracks.next();
			track.clear();
		}
		this.tracks.clear();
		this.channels.clear();
		this.measureHeaders.clear();
	}

	public TGSong clone(TGFactory factory){
		TGSong song = factory.newSong();
		copy(factory,song);
		return song;
	}

	public void copy(TGFactory factory,TGSong song){
		song.clear();
		song.setName(getName());
		song.setArtist(getArtist());
		song.setAlbum(getAlbum());
		song.setAuthor(getAuthor());
		song.setDate(getDate());
		song.setCopyright(getCopyright());
		song.setWriter(getWriter());
		song.setTranscriber(getTranscriber());
		song.setComments(getComments());
		Iterator<TGMeasureHeader> headers = getMeasureHeaders();
		while(headers.hasNext()){
			TGMeasureHeader header = headers.next();
			song.addMeasureHeader(header.clone(factory));
		}
		Iterator<TGChannel> channels = getChannels();
		while(channels.hasNext()){
			TGChannel channel = channels.next();
			song.addChannel(channel.clone(factory));
		}
		Iterator<TGTrack> tracks = getTracks();
		while(tracks.hasNext()){
			TGTrack track = tracks.next();
			song.addTrack(track.clone(factory, song));
		}
	}
}
