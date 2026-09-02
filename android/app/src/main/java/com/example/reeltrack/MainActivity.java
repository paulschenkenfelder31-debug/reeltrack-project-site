package com.example.reeltrack;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int BG=0xFF0B0D12, SURFACE=0xFF151821, SURFACE2=0xFF1D2130;
    private static final int TEXT=0xFFF4F3F8, MUTED=0xFF9A9CAA, PURPLE=0xFF9B7CFF, GREEN=0xFFA3FF6F, GOLD=0xFFFFC857;
    private static final String REELTRACK_API="https://reeltrack-film-tracker-site.paul-schenkenf115523.chatgpt.site/api/tmdb";
    private static final String TMDB_IMAGE="https://image.tmdb.org/t/p/w500";
    private static final Map<Integer,String> TMDB_GENRES=createGenreMap();
    private SharedPreferences prefs;
    private LinearLayout root, content, nav;
    private String currentTab="Home", lastQuery="";
    private Movie detailMovie;
    private boolean searching=false;
    private String networkMessage="Search thousands of films from the live database.";
    private final List<Movie> remoteResults=new ArrayList<Movie>();
    private final Map<String,Bitmap> imageCache=new HashMap<String,Bitmap>();
    private final List<Movie> featured=Arrays.asList(
        new Movie("dune2","Dune: Part Two","2024","science fiction film","Denis Villeneuve","","tt15239678","A mythic journey across Arrakis."),
        new Movie("pastlives","Past Lives","2023","romance film","Celine Song","","tt13238346","A tender story about memory, choice and connection."),
        new Movie("holdovers","The Holdovers","2023","comedy-drama","Alexander Payne","","tt14849194","An unlikely trio share a snowy holiday."),
        new Movie("anatomy","Anatomy of a Fall","2023","legal drama","Justine Triet","","tt17009710","A gripping examination of truth and intimacy."),
        new Movie("perfectdays","Perfect Days","2023","drama film","Wim Wenders","","tt27503384","A quiet celebration of routine and wonder."),
        new Movie("boyheron","The Boy and the Heron","2023","animated fantasy","Hayao Miyazaki","","tt6587046","A dreamlike voyage into another world.")
    );

    @Override public void onCreate(Bundle state){ super.onCreate(state); prefs=getSharedPreferences("reeltrack",MODE_PRIVATE); getWindow().setStatusBarColor(BG); if(!prefs.getBoolean("signed_in",false)) welcome(); else app("Home"); }

    private void welcome(){
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(BG);
        LinearLayout box=column(); box.setGravity(Gravity.CENTER_HORIZONTAL); box.setPadding(dp(26),dp(52),dp(26),dp(30)); scroll.addView(box,matchWrap());
        TextView logo=text("R",38,Color.WHITE,true); logo.setGravity(Gravity.CENTER); logo.setBackground(gradient(PURPLE,0xFF6D42E8,24)); box.addView(logo,new LinearLayout.LayoutParams(dp(82),dp(82)));
        TextView title=text("ReelTrack",32,TEXT,true); title.setPadding(0,dp(20),0,dp(5)); box.addView(title); box.addView(text("Your personal cinema companion",15,MUTED,false));
        TextView lead=text("Discover live movie data, build your watchlist, keep a diary, rate films and write personal reviews.",15,MUTED,false); lead.setGravity(Gravity.CENTER); lead.setPadding(dp(8),dp(18),dp(8),dp(26)); box.addView(lead);
        final EditText name=input("Your name",InputType.TYPE_CLASS_TEXT); box.addView(name,field());
        final EditText email=input("Email address",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS); box.addView(email,field());
        final EditText pass=input("Password (4+ characters)",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); box.addView(pass,field());
        Button go=button("Create local account",PURPLE,Color.WHITE); LinearLayout.LayoutParams gp=field(); gp.topMargin=dp(8); box.addView(go,gp);
        go.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ String n=name.getText().toString().trim(),e=email.getText().toString().trim(); if(n.length()<1||!e.contains("@")||pass.length()<4){toast("Enter your name, a valid email and a 4+ character password");return;} prefs.edit().putBoolean("signed_in",true).putString("name",n).putString("email",e).apply(); app("Home"); }});
        TextView local=text("Private by design • Your tracking data stays on this device",12,MUTED,false); local.setPadding(0,dp(17),0,0); box.addView(local); setContentView(scroll);
    }

    private void app(String tab){ currentTab=tab; root=column(); root.setBackgroundColor(BG); ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); content=column(); scroll.addView(content,matchWrap()); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); nav=new LinearLayout(this); nav.setOrientation(LinearLayout.HORIZONTAL); nav.setGravity(Gravity.CENTER); nav.setPadding(dp(2),dp(5),dp(2),dp(7)); nav.setBackgroundColor(SURFACE); root.addView(nav,new LinearLayout.LayoutParams(-1,dp(68))); setContentView(root); render(); }
    private void render(){ content.removeAllViews(); nav.removeAllViews(); if(detailMovie!=null) detail(detailMovie); else if(currentTab.equals("Home"))home(); else if(currentTab.equals("Explore"))explore(); else if(currentTab.equals("Library"))library(); else if(currentTab.equals("Diary"))diary(); else profile(); drawNav(); }
    private void drawNav(){ String[] tabs={"Home","Explore","Library","Diary","Profile"}; String[] icons={"⌂","⌕","▣","✓","●"}; for(int i=0;i<tabs.length;i++){ final String tab=tabs[i]; boolean on=detailMovie==null&&currentTab.equals(tab); TextView t=text(icons[i]+"\n"+tab,10,on?PURPLE:MUTED,on); t.setGravity(Gravity.CENTER); t.setOnClickListener(new View.OnClickListener(){public void onClick(View v){detailMovie=null;currentTab=tab;render();}}); nav.addView(t,new LinearLayout.LayoutParams(0,-1,1)); } }

    private void home(){
        top("Good evening, "+firstName(),"Find something unforgettable");
        LinearLayout stats=new LinearLayout(this); stats.setOrientation(LinearLayout.HORIZONTAL); LinearLayout.LayoutParams sp=wide(); sp.bottomMargin=dp(18); content.addView(stats,sp);
        stats.addView(stat(String.valueOf(set("watched").size()),"watched"),new LinearLayout.LayoutParams(0,dp(82),1)); gap(stats,8); stats.addView(stat(String.valueOf(set("watchlist").size()),"saved"),new LinearLayout.LayoutParams(0,dp(82),1)); gap(stats,8); stats.addView(stat(avgRating(),"avg rating"),new LinearLayout.LayoutParams(0,dp(82),1));
        TextView heroKicker=text("TONIGHT'S SPOTLIGHT",11,GREEN,true); heroKicker.setLetterSpacing(.12f); LinearLayout.LayoutParams kp=wide(); kp.bottomMargin=dp(8); content.addView(heroKicker,kp);
        Movie hero=featured.get(0); LinearLayout heroCard=column(); heroCard.setPadding(dp(20),dp(22),dp(20),dp(20)); heroCard.setBackground(gradient(0xFF382B68,0xFF1B1830,22)); heroCard.addView(text("DUNE: PART TWO",25,TEXT,true)); TextView hm=text("2024  •  SCI-FI  •  DENIS VILLENEUVE",11,0xFFD3C9FF,true); hm.setPadding(0,dp(7),0,dp(13)); heroCard.addView(hm); heroCard.addView(text("A mythic journey across Arrakis — epic scale, striking craft and a story worth revisiting.",14,0xFFE2DFF0,false)); Button hd=button("View film  →",Color.TRANSPARENT,GREEN); hd.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL); heroCard.addView(hd,new LinearLayout.LayoutParams(-1,dp(48))); hd.setOnClickListener(open(hero)); LinearLayout.LayoutParams hp=wide(); hp.bottomMargin=dp(23); content.addView(heroCard,hp);
        section("Curated for you","Hand-picked essentials"); HorizontalScrollView hs=new HorizontalScrollView(this); hs.setHorizontalScrollBarEnabled(false); LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setPadding(dp(20),0,dp(10),dp(8)); hs.addView(row); for(Movie m:featured){ LinearLayout tile=posterTile(m); LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(dp(154),dp(238)); tp.rightMargin=dp(12); row.addView(tile,tp); } content.addView(hs,new LinearLayout.LayoutParams(-1,dp(250)));
        Button live=button("Explore live movie database",PURPLE,Color.WHITE); LinearLayout.LayoutParams lp=wide(); lp.topMargin=dp(14); lp.bottomMargin=dp(24); content.addView(live,lp); live.setOnClickListener(new View.OnClickListener(){public void onClick(View v){currentTab="Explore";render();}});
    }

    private void explore(){
        top("Explore","Live movie search powered by TMDB");
        final EditText q=input("Search any film title",InputType.TYPE_CLASS_TEXT); q.setText(lastQuery); LinearLayout.LayoutParams qp=wide(); qp.bottomMargin=dp(10); content.addView(q,qp);
        Button search=button(searching?"Searching…":"Search movies",PURPLE,Color.WHITE); search.setEnabled(!searching); LinearLayout.LayoutParams bp=wide(); bp.bottomMargin=dp(14); content.addView(search,bp);
        search.setOnClickListener(new View.OnClickListener(){public void onClick(View v){String term=q.getText().toString().trim(); if(term.length()<2){toast("Type at least 2 characters");return;} lastQuery=term; searching=true; networkMessage="Searching the live film database…"; render(); new MovieSearchTask().execute(term);}});
        if(searching){ ProgressBar p=new ProgressBar(this); content.addView(p,new LinearLayout.LayoutParams(-1,dp(48))); }
        TextView msg=text(networkMessage,13,MUTED,false); LinearLayout.LayoutParams mp=wide(); mp.bottomMargin=dp(14); content.addView(msg,mp);
        if(remoteResults.size()>0){section("Results",remoteResults.size()+" films found"); for(Movie m:remoteResults)content.addView(movieCard(m),cardParams());}
        else if(!searching){ section("Try a search","Examples"); chips(new String[]{"Nosferatu","Dune","The Godfather","Spirited Away"},q); }
        attribution();
    }

    private void library(){
        top("Your library","Everything you want to remember"); Set<String> all=new HashSet<String>(); all.addAll(set("watchlist")); all.addAll(set("favorite")); all.addAll(set("watched"));
        LinearLayout counts=new LinearLayout(this); counts.setOrientation(LinearLayout.HORIZONTAL); LinearLayout.LayoutParams cp=wide(); cp.bottomMargin=dp(18); content.addView(counts,cp); counts.addView(stat(String.valueOf(set("watchlist").size()),"watchlist"),new LinearLayout.LayoutParams(0,dp(84),1)); gap(counts,8); counts.addView(stat(String.valueOf(set("favorite").size()),"favorites"),new LinearLayout.LayoutParams(0,dp(84),1)); gap(counts,8); counts.addView(stat(String.valueOf(set("watched").size()),"watched"),new LinearLayout.LayoutParams(0,dp(84),1));
        List<Movie> movies=known(all); if(movies.size()==0)empty("Your library is waiting","Search the live database and save your first film."); else for(Movie m:movies)content.addView(movieCard(m),cardParams());
    }

    private void diary(){
        top("Film diary","A timeline of what you watched"); List<Movie> watched=known(set("watched")); if(watched.size()==0){empty("No diary entries yet","Mark a film watched and it will appear here.");return;}
        for(Movie m:watched){ LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); TextView date=text(watchedDate(m.id),12,GREEN,true); date.setGravity(Gravity.CENTER); date.setBackground(round(SURFACE2,14)); row.addView(date,new LinearLayout.LayoutParams(dp(82),dp(62))); LinearLayout info=column(); info.setPadding(dp(14),0,0,0); info.addView(text(m.title,17,TEXT,true)); info.addView(text(ratingStars(rating(m.id))+"  "+m.year,13,rating(m.id)>0?GOLD:MUTED,false)); row.addView(info,new LinearLayout.LayoutParams(0,-2,1)); row.setOnClickListener(open(m)); LinearLayout.LayoutParams rp=wide(); rp.bottomMargin=dp(10); row.setLayoutParams(rp); content.addView(row); }
    }

    private void profile(){
        top("Profile","Your cinema identity"); LinearLayout card=column(); card.setGravity(Gravity.CENTER_HORIZONTAL); card.setPadding(dp(20),dp(25),dp(20),dp(25)); card.setBackground(round(SURFACE,22)); TextView avatar=text(initials(prefs.getString("name","Film Fan")),24,Color.WHITE,true); avatar.setGravity(Gravity.CENTER); avatar.setBackground(gradient(PURPLE,0xFF6845DF,40)); card.addView(avatar,new LinearLayout.LayoutParams(dp(76),dp(76))); TextView name=text(prefs.getString("name","Film Fan"),23,TEXT,true); name.setPadding(0,dp(12),0,dp(3)); card.addView(name); card.addView(text(prefs.getString("email",""),13,MUTED,false)); content.addView(card,wide());
        section("Your stats","Updated automatically"); LinearLayout grid=new LinearLayout(this); grid.setOrientation(LinearLayout.HORIZONTAL); LinearLayout.LayoutParams gp=wide(); gp.bottomMargin=dp(18); content.addView(grid,gp); grid.addView(stat(String.valueOf(set("watched").size()),"films"),new LinearLayout.LayoutParams(0,dp(96),1)); gap(grid,10); grid.addView(stat(avgRating(),"average"),new LinearLayout.LayoutParams(0,dp(96),1));
        LinearLayout source=column(); source.setPadding(dp(17),dp(16),dp(17),dp(16)); source.setBackground(round(SURFACE,16)); source.addView(text("MOVIE DATA BY TMDB",11,GREEN,true)); TextView sd=text("This product uses the TMDB API but is not endorsed or certified by TMDB. Your watchlist, ratings, diary and reviews remain on this device.",13,MUTED,false); sd.setPadding(0,dp(7),0,0); source.addView(sd); TextView tmdbLink=text("Visit themoviedb.org  ↗",12,PURPLE,true); tmdbLink.setPadding(0,dp(12),0,0); tmdbLink.setOnClickListener(new View.OnClickListener(){public void onClick(View v){startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.themoviedb.org")));}}); source.addView(tmdbLink); content.addView(source,wide());
        Button out=button("Sign out",SURFACE2,PURPLE); LinearLayout.LayoutParams op=wide(); op.topMargin=dp(20); op.bottomMargin=dp(25); content.addView(out,op); out.setOnClickListener(new View.OnClickListener(){public void onClick(View v){prefs.edit().putBoolean("signed_in",false).apply();welcome();}});
    }

    private void detail(final Movie m){
        TextView back=text("‹  Back to "+currentTab,14,PURPLE,true); back.setPadding(dp(20),dp(18),dp(20),dp(14)); back.setOnClickListener(new View.OnClickListener(){public void onClick(View v){detailMovie=null;render();}}); content.addView(back);
        LinearLayout hero=new LinearLayout(this); hero.setOrientation(LinearLayout.HORIZONTAL); hero.setPadding(dp(20),dp(20),dp(20),dp(20)); hero.setBackground(gradient(0xFF30264C,0xFF151821,0)); ImageView poster=posterView(m,dp(120),dp(176)); hero.addView(poster,new LinearLayout.LayoutParams(dp(120),dp(176))); LinearLayout info=column(); info.setPadding(dp(17),dp(4),0,0); info.addView(text(m.title,25,TEXT,true)); TextView meta=text(m.year+"  •  "+pretty(m.genre),13,0xFFCFC8E8,false); meta.setPadding(0,dp(7),0,dp(9)); info.addView(meta); info.addView(text("Directed by\n"+fallback(m.director,"Unknown"),13,MUTED,false)); TextView status=text(statusLine(m),12,GREEN,true); status.setPadding(0,dp(14),0,0); info.addView(status); hero.addView(info,new LinearLayout.LayoutParams(0,-2,1)); content.addView(hero,new LinearLayout.LayoutParams(-1,-2));
        TextView overview=text(fallback(m.summary,"Track this film, rate it after watching, and add your own review."),15,0xFFD4D3DA,false); overview.setLineSpacing(dp(3),1f); LinearLayout.LayoutParams ov=wide(); ov.topMargin=dp(20); ov.bottomMargin=dp(18); content.addView(overview,ov);
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL); LinearLayout.LayoutParams ap=wide(); ap.bottomMargin=dp(20); content.addView(actions,ap); actionButton(actions,m,"watchlist","+ Watchlist"); gap(actions,8); actionButton(actions,m,"watched","✓ Watched"); gap(actions,8); actionButton(actions,m,"favorite","♥ Favorite");
        section("Your rating","Tap a star"); LinearLayout stars=new LinearLayout(this); stars.setOrientation(LinearLayout.HORIZONTAL); stars.setGravity(Gravity.CENTER); for(int i=1;i<=5;i++){ final int value=i; Button star=button(i<=rating(m.id)?"★":"☆",Color.TRANSPARENT,i<=rating(m.id)?GOLD:MUTED); star.setTextSize(28); star.setPadding(0,0,0,0); star.setOnClickListener(new View.OnClickListener(){public void onClick(View v){remember(m);prefs.edit().putInt("rating."+m.id,value).apply();render();}}); stars.addView(star,new LinearLayout.LayoutParams(dp(56),dp(54))); } content.addView(stars,new LinearLayout.LayoutParams(-1,dp(58)));
        section("Personal review","Only visible to you"); final EditText note=new EditText(this); note.setText(prefs.getString("note."+m.id,"")); note.setHint("What did you think? Favorite scene, performance, feeling…"); note.setHintTextColor(MUTED); note.setTextColor(TEXT); note.setTextSize(14); note.setGravity(Gravity.TOP); note.setPadding(dp(15),dp(13),dp(15),dp(13)); note.setMinLines(4); note.setBackground(round(SURFACE,16)); LinearLayout.LayoutParams np=wide(); np.height=dp(120); np.bottomMargin=dp(10); content.addView(note,np); Button save=button("Save review",PURPLE,Color.WHITE); LinearLayout.LayoutParams sv=wide(); sv.bottomMargin=dp(12); content.addView(save,sv); save.setOnClickListener(new View.OnClickListener(){public void onClick(View v){remember(m);prefs.edit().putString("note."+m.id,note.getText().toString().trim()).apply();toast("Review saved");}});
        if(m.imdb.length()>0){Button imdb=button("Open IMDb page  ↗",SURFACE2,TEXT); LinearLayout.LayoutParams ip=wide(); ip.bottomMargin=dp(28); content.addView(imdb,ip); imdb.setOnClickListener(new View.OnClickListener(){public void onClick(View v){startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.imdb.com/title/"+m.imdb+"/")));}});}
    }

    private View.OnClickListener open(final Movie m){return new View.OnClickListener(){public void onClick(View v){remember(m);detailMovie=m;render();if(m.id.startsWith("tmdb:")&&!m.detailsLoaded&&!m.detailsLoading){m.detailsLoading=true;new MovieDetailsTask(m).execute();}}};}
    private LinearLayout movieCard(final Movie m){LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.HORIZONTAL);card.setPadding(dp(11),dp(11),dp(13),dp(11));card.setBackground(round(SURFACE,18));card.addView(posterView(m,dp(78),dp(112)),new LinearLayout.LayoutParams(dp(78),dp(112)));LinearLayout info=column();info.setPadding(dp(14),dp(2),0,0);info.addView(text(m.title,17,TEXT,true));TextView meta=text(m.year+"  •  "+pretty(m.genre),12,MUTED,false);meta.setPadding(0,dp(5),0,dp(7));info.addView(meta);info.addView(text("Director: "+fallback(m.director,"Unknown"),12,0xFFC1BFCA,false));TextView state=text(statusLine(m),11,GREEN,true);state.setPadding(0,dp(10),0,0);info.addView(state);card.addView(info,new LinearLayout.LayoutParams(0,-2,1));card.setOnClickListener(open(m));return card;}
    private LinearLayout posterTile(final Movie m){LinearLayout tile=column();tile.setPadding(dp(12),dp(12),dp(12),dp(12));tile.setBackground(round(SURFACE,18));tile.addView(posterView(m,dp(130),dp(155)),new LinearLayout.LayoutParams(-1,dp(155)));TextView title=text(m.title,14,TEXT,true);title.setMaxLines(2);title.setPadding(0,dp(9),0,dp(3));tile.addView(title);tile.addView(text(m.year+"  •  "+pretty(m.genre),10,MUTED,false));tile.setOnClickListener(open(m));return tile;}
    private ImageView posterView(Movie m,int w,int h){ImageView iv=new ImageView(this);iv.setScaleType(ImageView.ScaleType.CENTER_CROP);iv.setBackground(letterPoster(m));iv.setContentDescription(m.title+" poster");if(m.poster.length()>0){iv.setTag(m.poster);Bitmap b=imageCache.get(m.poster);if(b!=null)iv.setImageBitmap(b);else new PosterTask(iv).execute(m.poster);}return iv;}
    private GradientDrawable letterPoster(Movie m){int[] colors={0xFF533E85,0xFF315C63,0xFF7A3D54,0xFF6A5332,0xFF2F4E7A};return gradient(colors[Math.abs(m.title.hashCode())%colors.length],0xFF171923,14);}

    private void actionButton(LinearLayout row,final Movie m,final String key,String label){boolean on=set(key).contains(m.id);Button b=button(on?label+" ✓":label,on?PURPLE:SURFACE2,on?Color.WHITE:MUTED);b.setTextSize(10);b.setPadding(dp(5),0,dp(5),0);b.setOnClickListener(new View.OnClickListener(){public void onClick(View v){toggle(key,m);render();}});row.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));}
    private void toggle(String key,Movie m){remember(m);Set<String>s=set(key);boolean added=s.add(m.id);if(!added)s.remove(m.id);SharedPreferences.Editor e=prefs.edit().putStringSet(key,s);if(key.equals("watched")&&added)e.putLong("date."+m.id,System.currentTimeMillis());e.apply();}
    private void remember(Movie m){prefs.edit().putString("title."+m.id,m.title).putString("year."+m.id,m.year).putString("genre."+m.id,m.genre).putString("director."+m.id,m.director).putString("poster."+m.id,m.poster).putString("imdb."+m.id,m.imdb).putString("summary."+m.id,m.summary).apply();}
    private List<Movie> known(Set<String> ids){LinkedHashMap<String,Movie> map=new LinkedHashMap<String,Movie>();for(Movie m:featured)map.put(m.id,m);for(Movie m:remoteResults)map.put(m.id,m);List<Movie>out=new ArrayList<Movie>();for(String id:ids){Movie m=map.get(id);if(m==null)m=new Movie(id,prefs.getString("title."+id,"Unknown film"),prefs.getString("year."+id,""),prefs.getString("genre."+id,"film"),prefs.getString("director."+id,""),prefs.getString("poster."+id,""),prefs.getString("imdb."+id,""),prefs.getString("summary."+id,""));out.add(m);}return out;}

    private class MovieSearchTask extends AsyncTask<String,Void,List<Movie>>{
        String error="";
        protected List<Movie> doInBackground(String... terms){List<Movie>out=new ArrayList<Movie>();try{String url=REELTRACK_API+"/search?query="+URLEncoder.encode(terms[0],"UTF-8");JSONObject json=requestJson(url);JSONArray rows=json.getJSONArray("results");for(int i=0;i<rows.length()&&i<20;i++){JSONObject r=rows.getJSONObject(i);String id="tmdb:"+r.optInt("id"),title=r.optString("title",r.optString("original_title","Untitled")),date=r.optString("release_date",""),year=date.length()>=4?date.substring(0,4):"",posterPath=r.optString("poster_path",""),poster=posterPath.length()>0?TMDB_IMAGE+posterPath:"",summary=r.optString("overview","");out.add(new Movie(id,title,year,genreNames(r.optJSONArray("genre_ids")),"",poster,"",summary));}}catch(Exception ex){error=ex.getClass().getSimpleName()+": "+ex.getMessage();}return out;}
        protected void onPostExecute(List<Movie> result){searching=false;remoteResults.clear();remoteResults.addAll(result);networkMessage=error.length()>0?"Could not reach ReelTrack movie search. Check your connection and try again.":(result.size()==0?"No film matches found. Try the full title.":"Live results from TMDB • tap a film for full details");if(currentTab.equals("Explore")&&detailMovie==null)render();}
    }
    private class MovieDetailsTask extends AsyncTask<Void,Void,Boolean>{Movie movie;MovieDetailsTask(Movie m){movie=m;}protected Boolean doInBackground(Void...unused){try{String numericId=movie.id.substring("tmdb:".length());JSONObject json=requestJson(REELTRACK_API+"/movie/"+numericId);movie.summary=json.optString("overview",movie.summary);String date=json.optString("release_date","");if(date.length()>=4)movie.year=date.substring(0,4);String posterPath=json.optString("poster_path","");if(posterPath.length()>0)movie.poster=TMDB_IMAGE+posterPath;JSONArray genres=json.optJSONArray("genres");if(genres!=null&&genres.length()>0){List<String>names=new ArrayList<String>();for(int i=0;i<genres.length()&&i<3;i++)names.add(genres.getJSONObject(i).optString("name",""));movie.genre=join(names,", ");}JSONObject credits=json.optJSONObject("credits");if(credits!=null){JSONArray crew=credits.optJSONArray("crew");if(crew!=null)for(int i=0;i<crew.length();i++){JSONObject person=crew.optJSONObject(i);if(person!=null&&"Director".equals(person.optString("job"))){movie.director=person.optString("name","");break;}}}JSONObject external=json.optJSONObject("external_ids");if(external!=null)movie.imdb=external.optString("imdb_id","");return true;}catch(Exception e){return false;}}protected void onPostExecute(Boolean ok){movie.detailsLoading=false;movie.detailsLoaded=ok;remember(movie);if(detailMovie==movie)render();}}
    private JSONObject requestJson(String url) throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();try{c.setConnectTimeout(20000);c.setReadTimeout(30000);c.setRequestProperty("Accept","application/json");c.setRequestProperty("User-Agent","ReelTrack-Android/3.0");int status=c.getResponseCode();if(status<200||status>=300)throw new IllegalStateException("Movie service returned "+status);BufferedReader br=new BufferedReader(new InputStreamReader(c.getInputStream(),"UTF-8"));StringBuilder json=new StringBuilder();String line;while((line=br.readLine())!=null)json.append(line);br.close();return new JSONObject(json.toString());}finally{c.disconnect();}}
    private String genreNames(JSONArray ids){if(ids==null||ids.length()==0)return "film";List<String>names=new ArrayList<String>();for(int i=0;i<ids.length()&&i<3;i++){String name=TMDB_GENRES.get(ids.optInt(i));if(name!=null)names.add(name);}return names.size()==0?"film":join(names,", ");}
    private static Map<Integer,String> createGenreMap(){Map<Integer,String>g=new HashMap<Integer,String>();g.put(28,"Action");g.put(12,"Adventure");g.put(16,"Animation");g.put(35,"Comedy");g.put(80,"Crime");g.put(99,"Documentary");g.put(18,"Drama");g.put(10751,"Family");g.put(14,"Fantasy");g.put(36,"History");g.put(27,"Horror");g.put(10402,"Music");g.put(9648,"Mystery");g.put(10749,"Romance");g.put(878,"Science Fiction");g.put(10770,"TV Movie");g.put(53,"Thriller");g.put(10752,"War");g.put(37,"Western");return g;}
    private class PosterTask extends AsyncTask<String,Void,Bitmap>{ImageView target;String url;PosterTask(ImageView v){target=v;}protected Bitmap doInBackground(String...s){url=s[0];try{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(12000);c.setReadTimeout(18000);c.setRequestProperty("User-Agent","ReelTrack-Android/3.0");c.connect();Bitmap b=BitmapFactory.decodeStream(new BufferedInputStream(c.getInputStream()));c.disconnect();return b;}catch(Exception e){return null;}}protected void onPostExecute(Bitmap b){if(b!=null){imageCache.put(url,b);if(url.equals(target.getTag()))target.setImageBitmap(b);}}}

    private void top(String title,String sub){LinearLayout h=column();h.setPadding(dp(20),dp(23),dp(20),dp(18));h.addView(text(title,27,TEXT,true));TextView s=text(sub,13,MUTED,false);s.setPadding(0,dp(4),0,0);h.addView(s);content.addView(h);}
    private void section(String title,String sub){LinearLayout h=new LinearLayout(this);h.setOrientation(LinearLayout.HORIZONTAL);h.setGravity(Gravity.BOTTOM);h.setPadding(dp(20),dp(8),dp(20),dp(11));TextView t=text(title,19,TEXT,true);h.addView(t,new LinearLayout.LayoutParams(0,-2,1));h.addView(text(sub,11,MUTED,false));content.addView(h);}
    private void chips(String[] labels,final EditText field){HorizontalScrollView hs=new HorizontalScrollView(this);hs.setHorizontalScrollBarEnabled(false);LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setPadding(dp(20),0,dp(12),dp(14));for(String s:labels){final String label=s;Button b=button(s,SURFACE2,MUTED);b.setTextSize(12);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(42));p.rightMargin=dp(8);row.addView(b,p);b.setOnClickListener(new View.OnClickListener(){public void onClick(View v){field.setText(label);}});}hs.addView(row);content.addView(hs);}
    private void attribution(){TextView a=text("Powered by TMDB\nThis product uses the TMDB API but is not endorsed or certified by TMDB.",11,0xFF777986,false);a.setGravity(Gravity.CENTER);a.setPadding(dp(20),dp(20),dp(20),dp(30));a.setOnClickListener(new View.OnClickListener(){public void onClick(View v){startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.themoviedb.org")));}});content.addView(a);}
    private void empty(String title,String sub){LinearLayout e=column();e.setGravity(Gravity.CENTER);e.setPadding(dp(20),dp(45),dp(20),dp(45));e.setBackground(round(SURFACE,20));e.addView(text("◎",38,PURPLE,true));TextView t=text(title,19,TEXT,true);t.setPadding(0,dp(10),0,dp(6));e.addView(t);TextView s=text(sub,13,MUTED,false);s.setGravity(Gravity.CENTER);e.addView(s);content.addView(e,wide());}
    private LinearLayout stat(String value,String label){LinearLayout s=column();s.setGravity(Gravity.CENTER);s.setBackground(round(SURFACE,15));s.addView(text(value,21,TEXT,true));s.addView(text(label,10,MUTED,false));return s;}
    private void gap(LinearLayout row,int d){row.addView(new View(this),new LinearLayout.LayoutParams(dp(d),1));}
    private Button button(String label,int bg,int fg){Button b=new Button(this);b.setText(label);b.setTextColor(fg);b.setTextSize(14);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setAllCaps(false);b.setMinHeight(0);b.setMinWidth(0);b.setPadding(dp(12),0,dp(12),0);b.setBackground(round(bg,14));return b;}
    private EditText input(String hint,int type){EditText e=new EditText(this);e.setHint(hint);e.setHintTextColor(MUTED);e.setTextColor(TEXT);e.setTextSize(15);e.setSingleLine(true);e.setInputType(type);e.setPadding(dp(15),0,dp(15),0);e.setBackground(round(SURFACE,14));return e;}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private GradientDrawable round(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
    private GradientDrawable gradient(int a,int b,int radius){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});g.setCornerRadius(dp(radius));return g;}
    private LinearLayout.LayoutParams wide(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.leftMargin=dp(20);p.rightMargin=dp(20);return p;}
    private LinearLayout.LayoutParams cardParams(){LinearLayout.LayoutParams p=wide();p.bottomMargin=dp(11);return p;}
    private LinearLayout.LayoutParams field(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(55));p.bottomMargin=dp(11);return p;}
    private LinearLayout.LayoutParams matchWrap(){return new LinearLayout.LayoutParams(-1,-2);}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private Set<String> set(String key){return new HashSet<String>(prefs.getStringSet(key,new HashSet<String>()));}
    private int rating(String id){return prefs.getInt("rating."+id,0);}
    private String ratingStars(int r){if(r<1)return "Not rated";String s="";for(int i=0;i<r;i++)s+="★";return s;}
    private String avgRating(){Set<String>w=set("watched");int sum=0,n=0;for(String id:w){int r=rating(id);if(r>0){sum+=r;n++;}}return n==0?"—":String.format(Locale.US,"%.1f",sum/(float)n);}
    private String watchedDate(String id){long t=prefs.getLong("date."+id,0);return t==0?"WATCHED":new SimpleDateFormat("MMM dd\nyyyy",Locale.US).format(new Date(t)).toUpperCase(Locale.US);}
    private String statusLine(Movie m){List<String>s=new ArrayList<String>();if(set("watchlist").contains(m.id))s.add("WATCHLIST");if(set("watched").contains(m.id))s.add("WATCHED");if(set("favorite").contains(m.id))s.add("FAVORITE");if(rating(m.id)>0)s.add(ratingStars(rating(m.id)));return s.size()==0?"TAP FOR DETAILS":join(s,"  •  ");}
    private String join(List<String>s,String sep){String out="";for(String x:s)out+=out.length()==0?x:sep+x;return out;}
    private String firstName(){String n=prefs.getString("name","Film fan").trim();int x=n.indexOf(' ');return x>0?n.substring(0,x):n;}
    private String initials(String n){String[]p=n.trim().split("\\s+");return(p[0].substring(0,1)+(p.length>1?p[p.length-1].substring(0,1):"")).toUpperCase(Locale.US);}
    private String fallback(String s,String f){return s==null||s.trim().length()==0?f:s;}
    private String pretty(String s){s=fallback(s,"film");return s.substring(0,1).toUpperCase(Locale.US)+s.substring(1);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override public void onBackPressed(){if(detailMovie!=null){detailMovie=null;render();}else super.onBackPressed();}
    static class Movie{String id,title,year,genre,director,poster,imdb,summary;boolean detailsLoading=false,detailsLoaded=false;Movie(String i,String t,String y,String g,String d,String p,String imdbId,String sum){id=i;title=t;year=y;genre=g;director=d;poster=p;imdb=imdbId;summary=sum;detailsLoaded=!i.startsWith("tmdb:");}}
}
