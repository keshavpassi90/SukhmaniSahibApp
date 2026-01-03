package com.appoapp.sukhmanisahib.retrofit.wallpaper_model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WallPaperModel {

	@SerializedName("next_page")
	private String nextPage;

	@SerializedName("per_page")
	private int perPage;

	@SerializedName("page")
	private int page;

	@SerializedName("photos")
	private List<PhotosItem> photos;

	@SerializedName("total_results")
	private int totalResults;

	public void setNextPage(String nextPage){
		this.nextPage = nextPage;
	}

	public String getNextPage(){
		return nextPage;
	}

	public void setPerPage(int perPage){
		this.perPage = perPage;
	}

	public int getPerPage(){
		return perPage;
	}

	public void setPage(int page){
		this.page = page;
	}

	public int getPage(){
		return page;
	}

	public void setPhotos(List<PhotosItem> photos){
		this.photos = photos;
	}

	public List<PhotosItem> getPhotos(){
		return photos;
	}

	public void setTotalResults(int totalResults){
		this.totalResults = totalResults;
	}

	public int getTotalResults(){
		return totalResults;
	}
}